package org.yggdrasil.app.crispa.models.config

import com.google.gson.annotations.SerializedName

data class SwitchOptions (
	@SerializedName("maxTotalQueueSize") var maxTotalQueueSize : Int // Maximum size of all switch queues combined (in bytes).
)