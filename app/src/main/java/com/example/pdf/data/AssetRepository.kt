package com.example.pdf.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

class AssetRepository(private val context: Context) {

    private fun loadAssetGroups(): List<AssetGroup> {
        val jsonString: String
        try {
            jsonString = context.assets.open("asset_groups.json").bufferedReader().use { it.readText() }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            return emptyList()
        }
        val listType = object : TypeToken<List<AssetGroup>>() {}.type
        return Gson().fromJson(jsonString, listType) ?: emptyList()
    }

    fun getAssetGroups(): List<AssetGroup> {
        return loadAssetGroups()
    }

    fun getAssetGroup(groupId: String): AssetGroup? {
        return loadAssetGroups().find { it.id == groupId }
    }
}