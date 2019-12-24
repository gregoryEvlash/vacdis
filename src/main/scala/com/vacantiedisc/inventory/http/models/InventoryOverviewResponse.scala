package com.vacantiedisc.inventory.http.models

import com.vacantiedisc.inventory.models.InventoryResult

case class InventoryOverviewResponse(inventory: Seq[InventoryResult])