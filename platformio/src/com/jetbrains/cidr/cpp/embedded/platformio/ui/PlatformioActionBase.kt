package com.jetbrains.cidr.cpp.embedded.platformio.ui

import com.intellij.execution.process.BaseOSProcessHandler
import com.intellij.execution.ui.RunContentManager
import com.intellij.icons.AllIcons
import com.intellij.ide.BrowserUtil
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsContexts.TabTitle
import com.intellij.openapi.util.NlsSafe
import com.intellij.ui.LayeredIcon
import com.intellij.util.IconUtil
import com.jetbrains.cidr.cpp.embedded.platformio.ClionEmbeddedPlatformioBundle
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioConfigurable
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioService
import com.jetbrains.cidr.cpp.embedded.platformio.project.PlatfromioCliBuilder
import icons.ClionEmbeddedPlatformioIcons
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.Icon
import javax.swing.SwingConstants

private val NOTIFICATION_GROUP = NotificationGroupManager.getInstance().getNotificationGroup("PlatformIO plugin")

abstract class PlatformioActionBase(private  val text:  () -> @TabTitle String,
                                    val toolTip: @NlsSafe () -> String?,
                                    icon: Icon?) : DumbAwareAction(text, toolTip, icon) {

  protected fun actionPerformed(e: AnActionEvent,
                                reloadProject: Boolean,
                                appendEnvKey: Boolean,
                                verboseAllowed: Boolean,
                                vararg arguments: String) {
    val project = e.project
    if (project != null) {
      val commandLine = PlatfromioCliBuilder(true, project, appendEnvKey, verboseAllowed).withParams(*arguments).build()
      val runContentManager = RunContentManager.getInstance(project)
      val alreadyRunningDescriptor = runContentManager.allDescriptors.firstOrNull {
        val processHandler = it.processHandler as? BaseOSProcessHandler
        processHandler?.isProcessTerminated != true && processHandler?.commandLine == commandLine.commandLineString
      }
      if (alreadyRunningDescriptor == null) {
        val service = project.service<PlatformioService>()
        doRun(service, @Suppress("HardCodedStringLiteral") text.invoke(), commandLine, reloadProject)
      }
      else {
        runContentManager.getToolWindowByDescriptor(alreadyRunningDescriptor)?.activate(null)
        runContentManager.selectRunContent(alreadyRunningDescriptor)
      }
    }
  }

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

  companion object {
    private val badge = IconUtil.resizeSquared(ClionEmbeddedPlatformioIcons.Platformio, 8)

    fun pioIcon(icon: Icon): Icon {
      return LayeredIcon(2).apply {
        setIcon(icon, 0)
        setIcon(badge, 1, SwingConstants.SOUTH_EAST)
      }
    }
  }
}

class OpenSettings(private val project: Project?) : Runnable, ActionListener {
  override fun run() =
    ShowSettingsUtil.getInstance().showSettingsDialog(project, PlatformioConfigurable::class.java)

  override fun actionPerformed(e: ActionEvent) = run()
}

object OpenInstallGuide : Runnable, ActionListener {
  const val URL = "https://docs.platformio.org/en/latest/core/installation.html"
  override fun run() =
    BrowserUtil.browse(URL)

  override fun actionPerformed(e: ActionEvent) = run()
}

fun notifyPlatformioNotFound(project: Project?) {
  NOTIFICATION_GROUP
    .createNotification(ClionEmbeddedPlatformioBundle.message("platformio.not.found.title"),
                        ClionEmbeddedPlatformioBundle.message("platformio.not.found"), NotificationType.ERROR)
    .addAction(
      NotificationAction.createSimple(ClionEmbeddedPlatformioBundle.message("install.guide"), OpenInstallGuide))
    .apply { icon = AllIcons.General.ContextHelp }
    .addAction(
      NotificationAction.createSimpleExpiring(ClionEmbeddedPlatformioBundle.message("open.settings.link"), OpenSettings(project)))
    .notify(project)
}

fun notifyUploadUnavailable(project: Project?) {
  NOTIFICATION_GROUP
    .createNotification(ClionEmbeddedPlatformioBundle.message("notification.content.upload.unavailable"), NotificationType.WARNING)
    .notify(project)
}
