// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.gl.render

import javax.media.opengl.GL
import org.nlogo.api.{ DrawingInterface, World }

private class DrawingRenderer(world: World, drawing: DrawingInterface)
        extends TextureRenderer(world) with DrawingRendererInterface {

  def init(gl: GL) { }

  def renderDrawing(gl: GL) {
    calculateTextureSize(gl, drawing.isBlank)
    renderTexture(gl, drawing.isBlank)
  }

  private def renderTexture(gl: GL, blank: Boolean) {
    val width = drawing.getWidth
    val height = drawing.getHeight
    gl.glEnable(GL.GL_TEXTURE_2D);
    gl.glDisable(GL.GL_LIGHTING)
    gl.glEnable(GL.GL_BLEND)
    if(!blank) {
      if(newTexture) {
        if(texture != 0)
          gl.glDeleteTextures(1, java.nio.IntBuffer.wrap(Array[Int](texture)))
        texture = TextureUtils.genTexture(gl)
        gl.glBindTexture(GL.GL_TEXTURE_2D, texture)
        TextureUtils.makeTexture(gl, textureSize)
        drawing.markDirty()
        newTexture = false
      }
      else
        gl.glBindTexture(GL.GL_TEXTURE_2D, texture);
      gl.glPushMatrix()
      TextureUtils.setParameters(gl)
      gl.glBlendFunc(GL.GL_SRC_ALPHA,GL.GL_ONE_MINUS_SRC_ALPHA);
      renderTextureTiles(gl, width, height, textureSize,
                         drawing.colors, drawing.isDirty)
      drawing.markClean()
      gl.glPopMatrix()
    }
    gl.glEnable(GL.GL_LIGHTING)
    gl.glDisable(GL.GL_BLEND)
    gl.glBindTexture(GL.GL_TEXTURE_2D, 0)
  }

  private var drawingWidth = 0
  private var drawingHeight = 0

  private def calculateTextureSize(gl: GL, blank: Boolean) {
    var newSize = 0
    if(!blank && (drawing.getWidth != drawingWidth || drawing.getHeight != drawingHeight) ||
       textureSize == 0) {
      newSize = TextureUtils.calculateTextureSize(
        gl, drawing.getWidth, drawing.getHeight)
      newTexture = true
      textureSize = newSize
      tiles = TextureUtils.createTileArray(drawing.getWidth, drawing.getHeight, textureSize)
      drawingWidth = drawing.getWidth
      drawingHeight = drawing.getHeight
    }
  }

  def clear() {
    deleteTexture()
  }

}
