package io.zoemeow.dutschedule.model.helpandexternallink

data class HelpLinkInfo(
    val title: String,
    val description: String? = null,
    val url: String,
    val tag: String? = null
) {
    companion object {
        fun getAllExternalLink(): List<HelpLinkInfo> {
            return listOf(
                HelpLinkInfo(
                    title = "DUT Official Home Page",
                    description = "The official DUT home page",
                    tag = "official",
                    url = "http://dut.udn.vn"
                ),
                HelpLinkInfo(
                    title = "DUT Student Information System",
                    description = "The official DUT student page.",
                    tag = "official",
                    url = "http://sv.dut.udn.vn"
                ),
                HelpLinkInfo(
                    title = "Forum link",
                    description = "A area which student can leave a question or a request.",
                    tag = "official",
                    url = "https://fr.dut.udn.vn"
                ),
                HelpLinkInfo(
                    title = "School rule documents",
                    description = "All documents about school rules and regulations.",
                    tag = "official",
                    url = "https://1drv.ms/u/s!AtwKlDZ6Vqbto10bhHc0K7seyNGr?eaCTb8x"
                ),
                HelpLinkInfo(
                    title = "DUT Library",
                    description = null,
                    tag = "official",
                    url = "http://lib.dut.udn.vn"
                ),
                HelpLinkInfo(
                    title = "DUT on Facebook - Official",
                    description = "The official facebook link for DUT",
                    tag = "social",
                    url = "https://www.facebook.com/bachkhoaDUT"
                ),
                HelpLinkInfo(
                    title = "DUT on Facebook - Department of Student Affairs",
                    description = "A DUT facebook page for official update about school and its social.",
                    tag = "social",
                    url = "https://www.facebook.com/ctsvdhbkdhdn"
                )
            )
        }
    }
}