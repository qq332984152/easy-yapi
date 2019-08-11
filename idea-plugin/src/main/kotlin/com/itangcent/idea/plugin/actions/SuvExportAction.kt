package com.itangcent.idea.plugin.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.project.Project
import com.itangcent.common.exporter.ClassExporter
import com.itangcent.common.exporter.RequestHelper
import com.itangcent.idea.plugin.DataEventCollector
import com.itangcent.idea.plugin.api.export.EasyApiConfigReader
import com.itangcent.idea.plugin.api.export.DefaultRequestHelper
import com.itangcent.idea.plugin.api.export.SimpleClassExporter
import com.itangcent.idea.plugin.api.export.suv.SuvApiExporter
import com.itangcent.idea.plugin.script.GroovyActionExtLoader
import com.itangcent.idea.plugin.script.LoggerBuffer
import com.itangcent.intellij.config.ConfigReader
import com.itangcent.intellij.context.ActionContext
import com.itangcent.intellij.extend.guice.singleton
import com.itangcent.intellij.extend.guice.with
import com.itangcent.intellij.file.DefaultLocalFileRepository
import com.itangcent.intellij.file.LocalFileRepository
import com.itangcent.intellij.logger.Logger

class SuvExportAction : ApiExportAction("Export Api") {

    private var dataEventCollector: DataEventCollector? = null

    override fun actionPerformed(anActionEvent: AnActionEvent) {
        dataEventCollector = DataEventCollector(anActionEvent)

        load(dataEventCollector!!)

        dataEventCollector!!.disableDataReach()

        super.actionPerformed(anActionEvent)
    }

    override fun afterBuildActionContext(event: AnActionEvent, builder: ActionContext.ActionContextBuilder) {

        super.afterBuildActionContext(event, builder)

        val copyDataEventCollector = dataEventCollector
        builder.bind(DataContext::class) { it.toInstance(copyDataEventCollector) }

        builder.bind(ClassExporter::class) { it.with(SimpleClassExporter::class).singleton() }

        builder.bind(LocalFileRepository::class) { it.with(DefaultLocalFileRepository::class).singleton() }

        builder.bind(RequestHelper::class) { it.with(DefaultRequestHelper::class).singleton() }

        builder.bind(ConfigReader::class) { it.with(EasyApiConfigReader::class).singleton() }

        builder.bind(SuvApiExporter::class) { it.singleton() }

        builder.cache("DATA_EVENT_COLLECTOR", dataEventCollector)

        dataEventCollector = null

    }

    private fun load(dataContext: DataContext) {
        dataContext.getData(CommonDataKeys.PROJECT)
        dataContext.getData(CommonDataKeys.PSI_FILE)
        dataContext.getData(CommonDataKeys.NAVIGATABLE_ARRAY)
        dataContext.getData(CommonDataKeys.NAVIGATABLE)
        dataContext.getData(CommonDataKeys.EDITOR)
    }

    override fun actionPerformed(actionContext: ActionContext, project: Project?, anActionEvent: AnActionEvent) {
        super.actionPerformed(actionContext, project, anActionEvent)
        val multipleApiExporter = actionContext.instance(SuvApiExporter::class)
        multipleApiExporter.setCustomActionExtLoader { actionName, actionContextBuilder ->
            loadCustomActionExt(actionName, actionContext.instance(DataContext::class), actionContextBuilder)
        }
        multipleApiExporter.showExportWindow()
    }
}
