/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/

package org.apache.ofbiz.shipment.verify;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.List;
import java.util.LinkedList;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.GeneralException;
import org.apache.ofbiz.base.util.UtilGenerics;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.ServiceUtil;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.util.EntityQuery;

public class VerifyPickServices {

    private static BigDecimal ZERO = BigDecimal.ZERO;
	public static final String module = VerifyPickServices.class.getName();
    public static Map<String, Object> verifySingleItem(DispatchContext dctx, Map<String, ? extends Object> context) {
        Locale locale = (Locale) context.get("locale");
        VerifyPickSession pickSession = (VerifyPickSession) context.get("verifyPickSession");
        String orderId = (String) context.get("orderId");
        String shipGroupSeqId = (String) context.get("shipGroupSeqId");
        String productId = (String) context.get("productId");
        String originGeoId = (String) context.get("originGeoId");
        BigDecimal quantity = (BigDecimal) context.get("quantity");
        if (quantity != null) {
            try {
                pickSession.createRow(orderId, null, shipGroupSeqId, productId, originGeoId, quantity, locale);
            } catch (GeneralException e) {
                return ServiceUtil.returnError(e.getMessage());
            }
        }
        return ServiceUtil.returnSuccess();
    }

    public static Map<String, Object> verifyBulkItem(DispatchContext dctx, Map<String, ? extends Object> context) {
        Locale locale = (Locale) context.get("locale");
        VerifyPickSession pickSession = (VerifyPickSession) context.get("verifyPickSession");
        String orderId = (String) context.get("orderId");
        String shipGroupSeqId = (String) context.get("shipGroupSeqId");
        Map<String, ?> selectedMap = UtilGenerics.checkMap(context.get("selectedMap"));
        Map<String, String> itemMap = UtilGenerics.checkMap(context.get("itemMap"));
        Map<String, String> productMap = UtilGenerics.checkMap(context.get("productMap"));
        Map<String, String> originGeoIdMap = UtilGenerics.checkMap(context.get("originGeoIdMap"));
        Map<String, String> quantityMap = UtilGenerics.checkMap(context.get("quantityMap"));
        if (selectedMap != null) {
            for (String rowKey : selectedMap.keySet()) {
                String orderItemSeqId = itemMap.get(rowKey);
                String productId = productMap.get(rowKey);
                String originGeoId = originGeoIdMap.get(rowKey);
                String quantityStr = quantityMap.get(rowKey);
                if (UtilValidate.isNotEmpty(quantityStr)) {
                    BigDecimal quantity = new BigDecimal(quantityStr);
                    if (quantity.compareTo(ZERO) > 0) {
                        try {
                            pickSession.createRow(orderId, orderItemSeqId, shipGroupSeqId, productId, originGeoId, quantity, locale);
                        } catch (Exception ex) {
                            return ServiceUtil.returnError(ex.getMessage());
                        }
                    }
                }
            }
        }
        return ServiceUtil.returnSuccess();
    }

    public static Map<String, Object> completeVerifiedPick(DispatchContext dctx, Map<String, ? extends Object> context) {
        Locale locale = (Locale) context.get("locale");
		String shipmentId = null;
        VerifyPickSession pickSession = (VerifyPickSession) context.get("verifyPickSession");
       
		String orderId = (String) context.get("orderId");
        try {
            shipmentId = pickSession.complete(orderId, locale);
            Map<String, Object> shipment = new HashMap<String, Object>();
            shipment.put("shipmentId", shipmentId);
            pickSession.clearAllRows();
            return shipment;
        } catch (GeneralException ex) {
            return ServiceUtil.returnError(ex.getMessage(), ex.getMessageList());
        }
    }

    public static Map<String, Object> cancelAllRows(DispatchContext dctx, Map<String, ? extends Object> context) {
        VerifyPickSession session = (VerifyPickSession) context.get("verifyPickSession");
        session.clearAllRows();
        return ServiceUtil.returnSuccess();
    }
	
	
	
 /*public static Map<String, Object> pickOrderAsync(DispatchContext dctx, Map<String, ? extends Object> context)
 {
 
	Delegator delegator = dctx.getDelegator();
	LocalDispatcher dispatcher = dctx.getDispatcher();
	
	
	String orderId = (String) context.get("orderId");
	String facilityId = (String) context.get("facilityId");
	String orderItemSeqId = (String) context.get("orderItemSeqId");
	String shipGroupSeqId = (String) context.get("shipGroupSeqId");
	String productId = (String) context.get("productId");
	String originGeoId = (String) context.get("originGeoId");
	String inventoryItemId = (String) context.get("inventoryItemId");
	String quantity = (String) context.get("quantity");
	
	BigDecimal quantity1 = new BigDecimal(quantity);
 
	List<VerifyPickSessionRow> result = new LinkedList<VerifyPickSessionRow>();
	VerifyPickSessionRow row1 = new VerifyPickSessionRow(orderId,orderItemSeqId,shipGroupSeqId,productId,originGeoId,inventoryItemId,quantity1);
	result.add(row1);
	
	GenericValue userLogin = delegator.makeValue("UserLogin");
		userLogin.set("userLoginId","admin");
		userLogin.set("currentPassword","{SHA}47b56994cbc2b6d10aa1be30f70165adb305a41a");

	VerifyPickSession pickSession = new VerifyPickSession(dispatcher, userLogin);
	pickSession.setFacilityId(facilityId);
	pickSession.pickRows  = result ;
	
	Map<String, Object> contextOp = new HashMap<String, Object>();
	contextOp.put("userLogin",userLogin);
	contextOp.put("orderId",orderId);
	contextOp.put("verifyPickSession",pickSession);
	contextOp.put("shipGroupSeqId",shipGroupSeqId);
	contextOp.put("facilityId",facilityId);
	
	Debug.logInfo(">>>>>>>>>>> FACILITY == "+ facilityId, module);
	 Map<String, Object> storecontext = null;

		Map res=null;
		try{
			storecontext = dispatcher.runSync("completeVerifiedPick", contextOp);
			res = ServiceUtil.returnSuccess("Picked Successfully");
		}
		catch(Exception e)
		{
			res = ServiceUtil.returnError("Error: " + e.toString());
		}
		
		return res;
 }*/
 
   public static Map<String, Object> pickOrderAsync(DispatchContext dctx, Map<String, ? extends Object> context){ 
	
	Delegator delegator = dctx.getDelegator();
	LocalDispatcher dispatcher = dctx.getDispatcher();
	String orderId = (String) context.get("orderId");
	String facilityId = (String) context.get("facilityId");
	String shipGroupSeqId = (String) context.get("shipGroupSeqId");
	GenericValue userLogin = delegator.makeValue("UserLogin");
				userLogin.set("userLoginId","admin");
				userLogin.set("currentPassword","{SHA}47b56994cbc2b6d10aa1be30f70165adb305a41a");
	List<VerifyPickSessionRow> result = new LinkedList<VerifyPickSessionRow>();
	NodeList pickLineList = (NodeList) context.get("pickLines");
	int size = pickLineList.getLength();
	Map res=null;
	for(int i = 0; i < size; i++){
			Element pickLine = (Element) pickLineList.item(i);
			String orderItemSeqId = pickLine.getAttribute("orderItemSeqId");
			String productId = pickLine.getAttribute("productId");
			String originGeoId = pickLine.getAttribute("originGeoId");
			String inventoryItemId = pickLine.getAttribute("inventoryItemId");
			String quantity = pickLine.getAttribute("quantity");
			//String shipGroupSeqId = pickLine.getAttribute("shipGroupSeqId");
			BigDecimal quantity1 = new BigDecimal(quantity);
			GenericValue orderItemShipGrpInvRes = null;
			try {
			  orderItemShipGrpInvRes = EntityQuery.use(delegator).from("OrderItemShipGrpInvRes").where("orderId", orderId,"inventoryItemId", inventoryItemId).queryOne();
			} catch (GenericEntityException e) {
				// TODO Auto-generated catch block
				return ServiceUtil.returnError(e.getMessage());
			}
			if(UtilValidate.isEmpty((orderItemShipGrpInvRes.getString("releaseToFacility")))){
				continue;
			}
			VerifyPickSessionRow rows = new VerifyPickSessionRow(orderId,orderItemSeqId,shipGroupSeqId,productId,originGeoId,inventoryItemId,quantity1);
			result.add(rows);
		}
		if(UtilValidate.isNotEmpty(result)){
			VerifyPickSession pickSession = new VerifyPickSession(dispatcher, userLogin);
			pickSession.setFacilityId(facilityId);
			pickSession.pickRows  = result ;
			Map<String, Object> contextOp = new HashMap<String, Object>();
			contextOp.put("userLogin",userLogin);
			contextOp.put("orderId",orderId);
			contextOp.put("verifyPickSession",pickSession);
			contextOp.put("shipGroupSeqId",shipGroupSeqId);
			contextOp.put("facilityId",facilityId);
			Debug.logInfo(">>>>>>>>>>> FACILITY == "+ facilityId, module);
			Map<String, Object> storecontext = null;
			 try{
				storecontext = dispatcher.runSync("completeVerifiedPick", contextOp);
				res = ServiceUtil.returnSuccess("Picked Successfully");
			 }catch(Exception e){
					res = ServiceUtil.returnError("Error: " + e.toString());
			 }
		}
		
		return res;
 	}
}
