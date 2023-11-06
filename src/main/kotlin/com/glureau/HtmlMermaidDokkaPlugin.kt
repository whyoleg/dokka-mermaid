package com.glureau

import org.jetbrains.dokka.base.*
import org.jetbrains.dokka.plugability.*

class HtmlMermaidDokkaPlugin : DokkaPlugin() {

    private val dokkaBase by lazy { plugin<DokkaBase>() }

    val codeBlockRenderer by extending {
        dokkaBase.htmlCodeBlockRenderers providing { ctx ->
            MermaidHtmlRenderer(configuration<HtmlMermaidDokkaPlugin, HtmlMermaidConfiguration>(ctx))
        }
    }

    val mermaidInstaller by extending {
        dokkaBase.htmlPreprocessors providing ::MermaidInstaller order {
            after(dokkaBase.assetsInstaller)
            before(dokkaBase.customResourceInstaller)
        }
    }

    init {
        println("Mermaid Plugin installed")
    }

    @OptIn(DokkaPluginApiPreview::class)
    override fun pluginApiPreviewAcknowledgement(): PluginApiPreviewAcknowledgement = PluginApiPreviewAcknowledgement
}
