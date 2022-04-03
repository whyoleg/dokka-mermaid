package com.glureau

import kotlinx.html.FlowContent
import kotlinx.html.code
import kotlinx.html.div
import kotlinx.html.id
import kotlinx.html.pre
import kotlinx.html.script
import kotlinx.html.unsafe
import org.jetbrains.dokka.base.renderers.html.HtmlRenderer
import org.jetbrains.dokka.pages.ContentCodeBlock
import org.jetbrains.dokka.pages.ContentPage
import org.jetbrains.dokka.pages.ContentText
import org.jetbrains.dokka.plugability.DokkaContext
import kotlin.math.absoluteValue
import kotlin.random.Random

open class MermaidHtmlRenderer(
    context: DokkaContext
) : HtmlRenderer(context) {

    private val mermaidDetectionList = listOf(
        // https://mermaid-js.github.io/mermaid/#/flowchart?id=flowchart-orientation
        "flowchart TB",
        "flowchart TD",
        "flowchart BT",
        "flowchart RL",
        "flowchart LR",
        // https://mermaid-js.github.io/mermaid/#/sequenceDiagram
        "sequenceDiagram",
        // https://mermaid-js.github.io/mermaid/#/classDiagram
        "classDiagram",
        // https://mermaid-js.github.io/mermaid/#/stateDiagram
        "stateDiagram",
        "stateDiagram-v2",
        // https://mermaid-js.github.io/mermaid/#/entityRelationshipDiagram
        "erDiagram",
        // https://mermaid-js.github.io/mermaid/#/user-journey
        "journey",
        // https://mermaid-js.github.io/mermaid/#/gantt
        "gantt",
        // https://mermaid-js.github.io/mermaid/#/pie
        "pie",
        "pie title .*",
        // https://mermaid-js.github.io/mermaid/#/requirementDiagram
        "requirementDiagram",

        // deduced from https://mermaid-js.github.io/mermaid/#/examples
        "graph TB",
        "graph TD",
        "graph BT",
        "graph RL",
        "graph LR",
    ).map { Regex(it) }

    override fun FlowContent.buildCodeBlock(code: ContentCodeBlock, pageContext: ContentPage) {
        var isMermaidGraph = code.language == "mermaid"
        if (!isMermaidGraph && code.language == "") { // Trying to guess if it's actually a Mermaid graph
            val firstLine = (code.children.firstOrNull() as? ContentText)?.text?.trim()
            if (firstLine != null) {
                isMermaidGraph = mermaidDetectionList.any { it.matches(firstLine) }
            }
        }
        if (isMermaidGraph) {
            val graphDef = code.children.filterIsInstance<ContentText>()
                .joinToString("\n") { it.text }
                .replace("\"", "\\\"")
            val rand = Random.nextLong().absoluteValue.toString(32)
            val mermaidContainerId = "mermaid-container-$rand"
            val mermaidTargetId = "mermaid-target-$rand"
            div("sample-container") {
                div {
                    id = mermaidContainerId
                }
            }
            val defaultTheme = "default"
            val darkTheme = "dark"
            script {
                unsafe {
                    +"""
                    |
                    |window.addEventListener('load', function() {
                    |  var graphDef =  `$graphDef`;
                    |  var container = document.getElementById('$mermaidContainerId');
                    |  container.innerHTML = '<div id="$mermaidTargetId"></div>';
                    |  var cb$rand = function(svgGraph) {
                    |    container.innerHTML = svgGraph;
                    |    // Trick to make the graph takes only the required height.
                    |    document.getElementById('$mermaidTargetId').removeAttribute('height')
                    |  };
                    |  var updateGraph$rand = function(isDarkMode) {
                    |    var theme = '$defaultTheme';
                    |    if (isDarkMode) {
                    |      theme = '$darkTheme';
                    |    }
                    |    mermaid.initialize({'theme': theme});
                    |    mermaid.mermaidAPI.render('$mermaidTargetId', graphDef, cb$rand);
                    |  }
                    |  updateGraph$rand(document.getElementsByClassName('theme-dark').length > 0);
                    |  
                    |  var themeToggleButton = document.getElementById('theme-toggle-button');
                    |  themeToggleButton.addEventListener('click', () => {
                    |    updateGraph$rand(document.getElementsByClassName('theme-dark').length == 0);
                    |  });
                    |});
                    """.trimMargin()
                }
            }
        } else {
            // TODO: Original code from HtmlRenderer, no idea how to override and use super of a member extension function...
            div("sample-container") {
                code(code.style.joinToString(" ") { it.toString().toLowerCase() }) {
                    attributes["theme"] = "idea"
                    pre {
                        code.children.forEach { buildContentNode(it, pageContext) }
                    }
                }
            }
        }
    }
}