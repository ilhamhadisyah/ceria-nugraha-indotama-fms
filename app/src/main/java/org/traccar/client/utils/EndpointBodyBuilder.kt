package org.traccar.client.utils

import org.traccar.client.data.model.ActivityModel

object EndpointBodyBuilder {

    fun body(data: ActivityModel): String {
        return "{\n" +
                "\"imei\": \"${data.imei}\",\n" +
                "\"session_parent_number\": ${data.sessionParentNumber},\n" +
                "\"session_child_number\":${data.sessionChildNumber} ,\n" +
                "\"activity_type\": \"${data.activityType}\",\n" +
                "\"action\": \"${data.action}\",\n" +
                "\"lat\": \"${data.lat}\",\n" +
                "\"long\": \"${data.long}\",\n" +
                "\"created_at\": \"${data.createdAt}\"\n" +
                "}"
    }

    fun bodyWithLoadingMaterial(data: ActivityModel): String {
        return "{\n" +
                "\"imei\": \"${data.imei}\",\n" +
                "\"session_parent_number\": ${data.sessionParentNumber},\n" +
                "\"session_child_number\":${data.sessionChildNumber} ,\n" +
                "\"activity_type\": \"${data.activityType}\",\n" +
                "\"loading_material\": \"${data.loadingMaterial}\",\n" +
                "\"action\": \"${data.action}\",\n" +
                "\"lat\": \"${data.lat}\",\n" +
                "\"long\": \"${data.long}\",\n" +
                "\"created_at\": \"${data.createdAt}\"\n" +
                "}"
    }
}