package org.yggdrasil.app.crispa

import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat

class AboutActivity:AppCompatActivity() {

    companion object {

        @JvmStatic var about = "<body style=\"background-color:#343334;\">\n" +
                "<p class=\"western\" align=\"center\">\n" +
                "<font style=\"font-size: 10pt\" color=\"#ffffff\" color=\"#ffffff\">\n" +
                "Github repo <a href=\"https://github.com/yggdrasil-network/yggdrasil-android\"><u>https://github.com/yggdrasil-network/yggdrasil-android</u></a>.\n" +
                "</font>\n" +
                "<p class=\"western\" ><font style=\"font-size: 14pt\" color=\"#ffffff\"><b>Introduction</b></font></p>\n" +
                "<p>\n" +
                "<font style=\"font-size: 10pt\" color=\"#ffffff\">Yggdrasil is an early-stage implementation of a fully end-to-end encrypted IPv6 network. I\n" +
                "t is lightweight, self-arranging, supported on multiple platforms and allows pretty much any IPv6-capable application to communicate securely with other Yggdrasil nodes.\n" +
                "Yggdrasil does not require you to have IPv6 Internet connectivity - it also works over IPv4.</font></p>\n" +
                "<p class=\"western\"><font style=\"font-size: 14pt\" color=\"#ffffff\"><b>Credit</b></font></p>\n" +
                "<p>\n" +
                "<font style=\"font-size: 10pt\" color=\"#ffffff\" >Developers: vikulin <a href=\"mailto:vadym.vikulin@gmail.com\">vadym.vikulin@gmail.com</a>, ChronosX88 <a href=\"mailto:chronosx88@gmail.com\">chronosx88@gmail.com</a></font></p>\n" +
                "\n" +
                "<p>\n" +
                "<font style=\"font-size: 10pt\" color=\"#ffffff\">QA: jorektheglitch <a href=\"mailto:entressi@yandex.ru\">entressi@yandex.ru</a>\n" +
                "</font></p>\n" +
                "\n" +
                "<font class=\"western\" style=\"font-size: 14pt\" color=\"#ffffff\"><b>Is Yggdrasil safe?</b></font>\n" +
                "<p>\n" +
                "<font style=\"font-size: 10pt\" color=\"#ffffff\">All traffic sent across the Yggdrasil network is encrypted end-to-end. Assuming that our crypto is solid, it cannot be decrypted or read by any intermediate nodes, and can only be decrypted by the recipient for which it was intended. However, please note that Yggdrasil has not been officially externally audited.\n" +
                "</font></p>\n" +
                "\n" +
                "<font class=\"western\" style=\"font-size: 14pt\" color=\"#ffffff\"><b>Is Yggdrasil stable?</b></font>\n" +
                "<p>\n" +
                "<font style=\"font-size: 10pt\" color=\"#ffffff\">Our official stance is that it is still alpha software. Expect things to not be wholly smooth, and expect to have to upgrade often to the latest builds. That said, there is a small community of users who have not experienced any stability problems so far. Yggdrasil very rarely crashes.\n" +
                "</font></p>\n" +
                "\n" +
                "<font class=\"western\" style=\"font-size: 14pt\" color=\"#ffffff\"><b>Is Yggdrasil anonymous?</b></font>\n" +
                "<p>\n" +
                "<font style=\"font-size: 10pt\" color=\"#ffffff\">It is not a goal of the Yggdrasil project to provide anonymity. Your direct peers may be able to determine your location if, for example, you are peering over the Internet.\n" +
                "</font></p>\n" +
                "\n" +
                "<font class=\"western\" style=\"font-size: 14pt\" color=\"#ffffff\"><b>Does Yggdrasil work on my platform?</b></font>\n" +
                "<p>\n" +
                "<font style=\"font-size: 10pt\" color=\"#ffffff\">Quite likely! Take a look at the <a href=\"https://yggdrasil-network.github.io/platforms.html\"><u>Platforms</u></a> page - you’ll find platform-specific notes there.\n" +
                "</font></p>\n" +
                "\n" +
                "<font class=\"western\" style=\"font-size: 14pt\" color=\"#ffffff\"><b>Does Yggdrasil require IPv6?</b></font>\n" +
                "<p>\n" +
                "<font style=\"font-size: 10pt\" color=\"#ffffff\">Your system must be IPv6-capable, which just about all modern operating systems are.\n" +
                "While Yggdrasil does transport only IPv6 traffic internally, you do not need an IPv6 internet connection to peer with other Yggdrasil users. You can peer with other Yggdrasil nodes over either IPv4 or IPv6.\n" +
                "</font>\n" +
                "<p>\n" +
                "<font class=\"western\" style=\"font-size: 14pt\" color=\"#ffffff\"><b>Will Yggdrasil conflict with my network routing?</b></font>\n" +
                "<p>\n" +
                "<font style=\"font-size: 10pt\" color=\"#ffffff\">Yggdrasil uses the 0200::/7 range, which is a range deprecated by the IETF. It has been deprecated since 2004, pending changes to an RFC which simply never materialised 14 years later. It was decided to use this range instead of fc00::/7 (which is more typically allocated to private networks) in order to prevent conflicts with existing ULA ranges.\n" +
                "As long as you are not using this deprecated address range on your network, you will not experience any routing conflicts.\n" +
                "</font>\n" +
                "<p>\n" +
                "<font class=\"western\" style=\"font-size: 14pt\" color=\"#ffffff\"><b>Can the network be crawled?</b></font>\n" +
                "<p>\n" +
                "<font style=\"font-size: 10pt\" color=\"#ffffff\">Currently it is possible to crawl the network to reveal the spanning tree relationships by querying nodes in the DHT. This is how <a href=\"http://51.15.204.214/\"><u>the network map</u></a> and popularity contest are generated today. However, it is considered a design weakness that this is even possible currently and will hopefully be fixed in the future.\n" +
                "</font></p>\n" +
                "\n" +
                "<font class=\"western\" style=\"font-size: 14pt\" color=\"#ffffff\"><b>Can I run a crawler?</b></font>\n" +
                "<p>\n" +
                "<font style=\"font-size: 10pt\" color=\"#ffffff\">Please don’t run your own network crawler. Crawlers generate a lot of network-wide protocol traffic and effect is amplified by each additional crawler. If you really really really want information about the network today, use an <a href=\"http://[301:4541:2f84:1188:216:3eff:feb6:65a3]:3001/nodeinfo.json\"><u>existing data source within the network</u></a> instead of crawling yourself.\n" +
                "</font></p>\n" +
                "\n" +
                "<font class=\"western\" style=\"font-size: 14pt\" color=\"#ffffff\"><b>I’ve just installed Yggdrasil and I can’t ping anyone. What have I missed?</b></font>\n" +
                "<p>\n" +
                "<font style=\"font-size: 10pt\" color=\"#ffffff\">Yggdrasil requires that you configure either a static peer to another Yggdrasil node, or that you discover another Yggdrasil node on the same subnet using multicast discovery (which is enabled by default). If you have not added or discovered any peers, you will not be able to reach beyond your own node.\n" +
                "You can check if you have any peers by running yggdrasilctl getPeers - peer on port 0 is your own node, ports 1 and above are your active peers.\n" +
                "Stuck for peers? Try adding a <a href=\"https://github.com/yggdrasil-network/public-peers\"><u>public peer</u></a>.\n" +
                "</font></p>\n" +
                "\n" +
                "<font class=\"western\" style=\"font-size: 14pt\" color=\"#ffffff\"><b>I’ve installed the Yggdrasil Debian package and now I can’t find the logs.</b></font>\n" +
                "<p>\n" +
                "<font style=\"font-size: 10pt\" color=\"#ffffff\">The Debian package installs the Yggdrasil service into systemd, therefore you can query systemd for the logs:\n" +
                "<p>\n" +
                "<font style=\"font-size: 10pt\" color=\"#ffffff\">\n" +
                "    systemctl status yggdrasil\n" +
                "</font>\n" +
                "<p>\n" +
                "<font style=\"font-size: 10pt\" color=\"#ffffff\">\n" +
                "    journalctl -u yggdrasil\n" +
                "</font<\n" +
                "</font></p>\n" +
                "\n" +
                "<font class=\"western\" style=\"font-size: 14pt\" color=\"#ffffff\"><b>I’ve modified the configuration file but nothing has changed.</b></font>\n" +
                "<p>\n" +
                "<font style=\"font-size: 10pt\" color=\"#ffffff\">Yggdrasil only loads the configuration at startup. Restart the Yggdrasil process or service to load the new configuration.\n" +
                "</font></p>\n" +
                "\n" +
                "<font class=\"western\" style=\"font-size: 14pt\" color=\"#ffffff\"><b>I’m running Yggdrasil on a machine that is directly reachable from the Internet. Does this mean anyone can peer with me?</b></font>\n" +
                "<p>\n" +
                "<font style=\"font-size: 10pt\" color=\"#ffffff\">Without any further configuration, yes.\n" +
                "However, you can either limit incoming connections to your host using a firewall by limiting or denying connections to the port specified in your Listen configuration option. This is useful if you want to limit peerings from certain IP ranges or on certain interfaces.\n" +
                "<p>\n" +
                "<font style=\"font-size: 10pt\" color=\"#ffffff\">\n" +
                "Alternatively, you can limit who can peer with you by modifying the AllowedEncryptionPublicKeys option in your Yggdrasil configuration. When this list is empty, any remote node is allowed to peer with you.\n" +
                "</font>\n" +
                "<p>\n" +
                "<font style=\"font-size: 10pt\" color=\"#ffffff\">\n" +
                "To restrict incoming peerings to certain nodes, you should first ask the operators of those nodes for their EncryptionPublicKey and then add those public keys into your own AllowedEncryptionPublicKeys list. From that point forward, only nodes with those public keys will be allowed to peer with you.\n" +
                "</font>\n" +
                "</font></p>\n" +
                "\n" +
                "<font class=\"western\" style=\"font-size: 14pt\" color=\"#ffffff\"><b>I am running Yggdrasil from behind a NAT. Will this affect my connectivity?</b></font>\n" +
                "<p>\n" +
                "<font style=\"font-size: 10pt\" color=\"#ffffff\">To accept incoming peerings, you will probably need to configure port forwarding on your router/gateway. Yggdrasil listens on the port number specified in the Listen setting, so forward this port to the machine that runs Yggdrasil.\n" +
                "<p>\n" +
                "<font style=\"font-size: 10pt\" color=\"#ffffff\">\n" +
                "To use outbound peerings, that is, static peers that have been configured in your Peers setting, you will likely not need to change anything.\n" +
                "</font>\n" +
                "</font></p>\n" +
                "\n" +
                "<font class=\"western\" style=\"font-size: 14pt\" color=\"#ffffff\"><b>Why does my Yggdrasil adapter have an unusually high MTU?</b></font>\n" +
                "<p>\n" +
                "<font style=\"font-size: 10pt\" color=\"#ffffff\">Yggdrasil peerings are typically stream-based and therefore don’t suffer from fragmentation issues when pushing large amounts of data. By using the largest possible MTU supported by a platform, we can send much more data in each write, and the TCP connection will take care of the rest. This also helps somewhat in the reduction of TCP-over-TCP amplification, as there are less control messages to be amplified.\n" +
                "<p>\n" +
                "<font style=\"font-size: 10pt\" color=\"#ffffff\">\n" +
                "This also uses less CPU, as we can send more data for every system call on the TUN/TAP adapter or network socket. System calls often result in context switches by the operating system and are expensive operations, therefore by using an MTU of up to 65535, we can save as many as 42 context switches for each packet - a substantial performance improvement!\n" +
                "</font>\n" +
                "</font></p>\n" +
                "\n" +
                "<font class=\"western\" style=\"font-size: 14pt\" color=\"#ffffff\"><b>I’ve changed my AdminListen port and now yggdrasilctl doesn’t work.</b></font>\n" +
                "<p>\n" +
                "<font style=\"font-size: 10pt\" color=\"#ffffff\">yggdrasilctl will assume that your admin port is on localhost:9001. If you have changed it, simply pass your configured endpoint through to yggdrasilctl, i.e.\n" +
                "<p>\n" +
                "<font style=\"font-size: 10pt\" color=\"#ffffff\">\n" +
                "yggdrasilctl -endpoint=127.0.0.1:12345\n" +
                "</font>\n" +
                "</font></p>\n" +
                "\n" +
                "<font class=\"western\" style=\"font-size: 14pt\" color=\"#ffffff\"><b>I want to run an Yggdrasil router to provide connectivity for other people, but I don’t want them to be able to reach my own machine.</b></font>\n" +
                "<p>\n" +
                "<font style=\"font-size: 10pt\" color=\"#ffffff\">You can set the IfName configuration setting to \"none\". This will load Yggdrasil, but will not create a TUN/TAP adapter, meaning that your host will not be exposed to the Yggdrasil network. This does of course mean that you won’t be able to send any traffic from that node to Yggdrasil either\n" +
                "</font></p>\n" +
                "\n" +
                "<font class=\"western\" style=\"font-size: 14pt\" color=\"#ffffff\"><b>Does Yggdrasil work alongside an existing VPN?</b></font>\n" +
                "<p>\n" +
                "<font style=\"font-size: 10pt\" color=\"#ffffff\">Yes, Yggdrasil should not interfere with existing VPNs. VPN traffic can be sent while using Yggdrasil.\n" +
                "</font></p>\n" +
                "\n" +
                "<font class=\"western\" style=\"font-size: 14pt\" color=\"#ffffff\"><b>Does Yggdrasil work with networks like Tor or I2P?</b></font>\n" +
                "<p>\n" +
                "<font style=\"font-size: 10pt\" color=\"#ffffff\">Yggdrasil can peer over Tor or I2P. See <a href=\"https://github.com/yggdrasil-network/public-peers/tree/master/other\"><u>/public-peers/tree/master/other</u></a> for public peers.\n" +
                "</font></p>\n" +
                "\n" +
                "<font class=\"western\" style=\"font-size: 14pt\" color=\"#ffffff\"><b>I want to allow outgoing connections from my machine but prevent unwanted incoming connections.</b></font>\n" +
                "<p>\n" +
                "<font style=\"font-size: 10pt\" color=\"#ffffff\">Generally this requires you to use a firewall. The steps for this will vary from platform to platform.\n" +
                "</font></p>\n" +
                "<font class=\"western\" style=\"font-size: 13pt\" color=\"#ffffff\"><b>Linux (with ip6tables)</b></font>\n" +
                "<p>\n" +
                "<font style=\"font-size: 10pt\" color=\"#ffffff\">Assuming your TUN/TAP adapter is tun0:\n" +
                "<p>\n" +
                "<font style=\"font-size: 10pt\" color=\"#ffffff\">\n" +
                "ip6tables -A INPUT -i tun0 -m conntrack --ctstate ESTABLISHED,RELATED -j ACCEPT\n" +
                "</font>\n" +
                "<p>\n" +
                "<font style=\"font-size: 10pt\" color=\"#ffffff\">\n" +
                "ip6tables -A INPUT -i tun0 -m conntrack --ctstate INVALID -j DROP\n" +
                "</font>\n" +
                "<p>\n" +
                "<font style=\"font-size: 10pt\" color=\"#ffffff\">\n" +
                "ip6tables -A INPUT -i tun0 -j DROP\n" +
                "</font>\n" +
                "</font></p>\n" +
                "\n" +
                "<font class=\"western\" style=\"font-size: 13pt\" color=\"#ffffff\"><b>Windows (with Windows Firewall)</b></font>\n" +
                "\n" +
                "<font style=\"font-size: 10pt\" color=\"#ffffff\">Windows, by default, will classify the TAP adapter as a “Public Network”. Configure Windows Firewall to prevent incoming connections on Public networks.\n" +
                "</font></p>\n" +
                "\n" +
                "<font class=\"western\" style=\"font-size: 13pt\" color=\"#ffffff\"><b>macOS (with built-in firewall)</b></font>\n" +
                "\n" +
                "<font style=\"font-size: 10pt\" color=\"#ffffff\">macOS has an application firewall, therefore any firewall policies applied on other interfaces will also apply to the Yggdrasil interface.\n" +
                "</font></p>\n" +
                "\n" +
                "<font class=\"western\" style=\"font-size: 14pt\" color=\"#ffffff\"><b>Is there any benefit to being the “root node” of the network?</b></font>\n" +
                "<p>\n" +
                "<font style=\"font-size: 10pt\" color=\"#ffffff\">No. At worst, the root node may be used in worst-case-scenario paths between other nodes in the absence of being able to determine better routes, but this is not advantageous.\n" +
                "</font></p>\n" +
                "</body>"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        setSupportActionBar(findViewById(R.id.toolbar))
        var textArea = this.findViewById<TextView>(R.id.about)
        textArea.movementMethod = LinkMovementMethod.getInstance();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            textArea.text =  Html.fromHtml(about, HtmlCompat.FROM_HTML_MODE_LEGACY)
        } else {
            textArea.text =  Html.fromHtml(about)
        }
    }
}