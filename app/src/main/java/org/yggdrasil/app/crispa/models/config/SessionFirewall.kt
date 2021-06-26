package org.yggdrasil.app.crispa.models.config

import com.google.gson.annotations.SerializedName

data class SessionFirewall (
	@SerializedName("enable") var enable : Boolean, // Enable or disable the session firewall. If disabled, network traffic from any node will be allowed. If enabled, the below rules apply.
	@SerializedName("allowFromDirect") var allowFromDirect : Boolean, // Allow network traffic from directly connected peers.
	@SerializedName("allowFromRemote") var allowFromRemote : Boolean, // Allow network traffic from remote nodes on the network that you are not directly peered with.
	@SerializedName("alwaysAllowOutbound") var alwaysAllowOutbound : Boolean, // Allow outbound network traffic regardless of AllowFromDirect or AllowFromRemote. This does allow a remote node to send unsolicited traffic back to you for the length of the session.
	@SerializedName("whitelistEncryptionPublicKeys") var whitelistEncryptionPublicKeys : List<String>, // List of public keys from which network traffic is always accepted, regardless of AllowFromDirect or AllowFromRemote.
	@SerializedName("blacklistEncryptionPublicKeys") var blacklistEncryptionPublicKeys : List<String> // List of public keys from which network traffic is always rejected, regardless of the whitelist, AllowFromDirect or AllowFromRemote.
)