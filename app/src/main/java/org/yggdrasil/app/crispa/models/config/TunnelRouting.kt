package org.yggdrasil.app.crispa.models.config

import com.google.gson.annotations.SerializedName

data class TunnelRouting (
	@SerializedName("enable") var enable : Boolean, // Enable or disable tunnel routing.
	@SerializedName("iPv6Destinations") var iPv6Destinations : Map<String, String>, // IPv6 CIDR subnets, mapped to the EncryptionPublicKey to which they should be routed, e.g. { "aaaa:bbbb:cccc::/e": "boxpubkey", ... }"
	@SerializedName("iPv6Sources") var iPv6Sources : List<String>, // Optional IPv6 source subnets which are allowed to be tunnelled in addition to this node's Yggdrasil address/subnet. If not specified, only traffic originating from this node's Yggdrasil address or subnet will be tunnelled.
	@SerializedName("iPv4Destinations") var iPv4Destinations : Map<String, String>, // IPv4 CIDR subnets, mapped to the EncryptionPublicKey to which they should be routed, e.g. { "a.b.c.d/e": "boxpubkey", ... }
	@SerializedName("iPv4Sources") var iPv4Sources : List<String> // IPv4 source subnets which are allowed to be tunnelled. Unlike for IPv6, this option is required for bridging IPv4 traffic. Only traffic with a source matching these subnets will be tunnelled.
)