package net.ccbluex.liquidbounce.ui.client.hud

import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.*
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.minecraft.client.gui.ScaledResolution
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.lwjgl.opengl.GL11
import kotlin.math.max
import kotlin.math.min

/**
 * LiquidBounce Hacked Client
 * A minecraft forge injection client using Mixin
 *
 * @game Minecraft
 * @author CCBlueX
 */
@SideOnly(Side.CLIENT)
open class HUD : MinecraftInstance() {

    val elements = mutableListOf<Element>()
    val notifications = mutableListOf<Notification>()

    companion object {

        val elements = arrayOf(
                Armor::class.java,
                Arraylist::class.java,
                Effects::class.java,
                Image::class.java,
                Model::class.java,
                Notifications::class.java,
                TabGUI::class.java,
                Text::class.java
        )

        /**
         * Create default HUD
         */
        @JvmStatic
        fun createDefault() = HUD()
                .addElement(Text.defaultClient())
                .addElement(TabGUI())
                .addElement(Arraylist())
                .addElement(ScoreboardElement())
                .addElement(Armor())
                .addElement(Effects())

    }

    /**
     * Render all elements
     */
    fun render(designer: Boolean) {
        for (element in elements) {
            GL11.glPushMatrix()
            GL11.glScalef(element.scale, element.scale, element.scale)
            GL11.glTranslated(element.renderX, element.renderY, 0.0)

            try {
                element.border = element.drawElement()

                if (designer)
                    element.border?.draw()
            } catch (ex: Exception) {
                ClientUtils.getLogger()
                        .error("Something went wrong while drawing ${element.name} element in HUD.", ex)
            }

            GL11.glPopMatrix()
        }
    }

    /**
     * Update all elements
     */
    fun update() {
        for (element in elements)
            element.updateElement()
    }

    /**
     * Handle mouse click
     */
    fun handleMouseClick(mouseX: Int, mouseY: Int, button: Int) {
        for (element in elements)
            element.handleMouseClick((mouseX / element.scale) - element.renderX, (mouseY / element.scale)
                    - element.renderY, button)

        if (button == 0) {
            for (element in elements.reversed()) {
                if (!element.isInBorder((mouseX / element.scale) - element.renderX,
                                (mouseY / element.scale) - element.renderY))
                    continue

                element.drag = true
                elements.remove(element)
                elements.add(element)
                break
            }
        }
    }

    /**
     * Handle released mouse key
     */
    fun handleMouseReleased() {
        for (element in elements)
            element.drag = false
    }

    /**
     * Handle mouse move
     */
    fun handleMouseMove(mouseX: Int, mouseY: Int) {
        if (mc.currentScreen !is GuiHudDesigner)
            return

        val scaledResolution = ScaledResolution(mc)

        for (element in elements) {
            val scaledX = mouseX / element.scale
            val scaledY = mouseY / element.scale
            val prevMouseX = element.prevMouseX
            val prevMouseY = element.prevMouseY

            element.prevMouseX = scaledX
            element.prevMouseY = scaledY

            if (element.drag) {
                val moveX = scaledX - prevMouseX
                val moveY = scaledY - prevMouseY

                if (moveX == 0F && moveY == 0F)
                    continue

                val border = element.border ?: Border(0F, 0F, 0F, 0F)

                val minX = min(border.x, border.x2)
                val minY = min(border.y, border.y2)

                val maxX = max(border.x, border.x2)
                val maxY = max(border.y, border.y2)

                if (element.renderX + minX + moveX >= 0.0 && element.renderX + maxX + moveX <= scaledResolution.scaledWidth / element.scale)
                    element.renderX = moveX.toDouble()
                if (element.renderY + minY + moveY >= 0.0 && element.renderY + maxY + moveY <= scaledResolution.scaledHeight / element.scale)
                    element.renderY = moveY.toDouble()
            }
        }
    }

    /**
     * Handle incoming key
     */
    fun handleKey(c: Char, keyCode: Int) {
        for (element in elements)
            element.handleKey(c, keyCode)
    }

    /**
     * Add [element] to HUD
     */
    fun addElement(element: Element): HUD {
        elements.add(element)
        element.updateElement()
        return this
    }

    /**
     * Remove [element] from HUD
     */
    fun removeElement(element: Element): HUD {
        element.destroyElement()
        elements.remove(element)
        return this
    }

    /**
     * Clear all elements
     */
    fun clearElements() {
        for (element in elements)
            element.destroyElement()

        elements.clear()
    }

    /**
     * Add [notification]
     */
    fun addNotification(notification: Notification) = elements.any { it is Notifications } && notifications.add(notification)

    /**
     * Remove [notification]
     */
    fun removeNotification(notification: Notification) = notifications.remove(notification)

}