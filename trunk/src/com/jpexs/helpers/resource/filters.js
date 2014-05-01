/**
 *  JPEXS Free Flash Decompiler Filters
 */     
        
Filters = {};        
        
Filters.createCanvas = function(width,height){
   var c = document.createElement("canvas");
   c.width = width;
   c.height = height;
   c.style.display="none";
   //temporary add to document to get this work (getImageData, etc.)
   document.body.appendChild(c);
   document.body.removeChild(c);
   return c;
};        
        
Filters._premultiply = function(data){
  var len = data.length;        
  for (var i = 0; i < len; i+=4) {     
      var f = data[i+3] * 0.003921569;
      data[i] = Math.round(data[i] * f);
      data[i+1] = Math.round(data[i+1] * f);
      data[i+2] = Math.round(data[i+2] * f);      
  }  
};

Filters._unpremultiply = function(data){
  var len = data.length;        
  for (var i = 0; i < len; i+=4) { 
      var a = data[i+3];
      if (a == 0 || a == 255) {
        continue;
      }    
      var f = 255/a;
      var r = (data[i] * f);
      var g = (data[i+1] * f);
      var b = (data[i+2] * f);
      if (r > 255) {
          r = 255;
      }
      if (g > 255) {
          g = 255;
      }
      if (b > 255) {
          b = 255;
      }
      
      data[i] = r;
      data[i+1] = g;
      data[i+2] = b;
  }  
};


Filters._boxBlurHorizontal = function(pixels, mask, w, h, radius) {
        var index = 0;
        var newColors = [];

        for (var y = 0; y < h; y++) {
            var hits = 0;
            var r = 0;
            var g = 0;
            var b = 0;
            var a = 0;
            for (var x = -radius*4; x < w*4; x+=4) {
                var oldPixel = x - radius*4 - 4;
                if (oldPixel >= 0) {
                    if ((mask == null) || (mask[index + oldPixel + 3] > 0)) {
                        a -= pixels[index + oldPixel + 3];
                        r -= pixels[index + oldPixel];
                        g -= pixels[index + oldPixel + 1];
                        b -= pixels[index + oldPixel + 2];
                        hits--;
                    }
                }

                var newPixel = x + radius*4;
                if (newPixel < w*4) {
                    if ((mask == null) || (mask[index + newPixel + 3] > 0)) {
                        a += pixels[index + newPixel + 3];
                        r += pixels[index + newPixel];
                        g += pixels[index + newPixel + 1];
                        b += pixels[index + newPixel + 2];
                        hits++;
                    }
                }

                if (x >= 0) {
                    if ((mask == null) || (mask[index + x + 3] > 0)) {
                        if (hits == 0) {
                            newColors[x] = 0;
                            newColors[x+1] = 0;
                            newColors[x+2] = 0;
                            newColors[x+3] = 0;
                        } else {
                            newColors[x] = Math.round(r / hits);
                            newColors[x+1] = Math.round(g / hits);
                            newColors[x+2] = Math.round(b / hits);
                            newColors[x+3] = Math.round(a / hits);

                        }
                    } else {
                        newColors[x] = 0;
                        newColors[x+1] = 0;
                        newColors[x+2] = 0;
                        newColors[x+3] = 0;
                    }
                }
            }
            for(var p=0;p<w*4;p+=4){
              pixels[index + p] = newColors[p];
              pixels[index + p + 1] = newColors[p + 1];
              pixels[index + p + 2] = newColors[p + 2];
              pixels[index + p + 3] = newColors[p + 3];
            }

            index += w*4;
        }
    };

Filters._boxBlurVertical = function(pixels, mask, w, h, radius) {
        var newColors = [];
        var oldPixelOffset = -(radius + 1) * w * 4;
        var newPixelOffset = (radius) * w * 4;

        for (var x = 0; x < w*4; x+=4) {
            var hits = 0;
            var r = 0;
            var g = 0;
            var b = 0;
            var a = 0;
            var index = -radius * w * 4 + x;
            for (var y = -radius; y < h; y++) {
                var oldPixel = y - radius - 1;
                if (oldPixel >= 0) {
                    if ((mask == null) || (mask[index + oldPixelOffset + 3] > 0)) {
                        a -= pixels[index + oldPixelOffset + 3];
                        r -= pixels[index + oldPixelOffset];
                        g -= pixels[index + oldPixelOffset + 1];
                        b -= pixels[index + oldPixelOffset + 2];                        
                        hits--;
                    }

                }

                var newPixel = y + radius;
                if (newPixel < h) {
                    if ((mask == null) || (mask[index + newPixelOffset + 3] > 0)) {
                        a += pixels[index + newPixelOffset + 3];
                        r += pixels[index + newPixelOffset];
                        g += pixels[index + newPixelOffset + 1];
                        b += pixels[index + newPixelOffset + 2];
                        hits++;
                    }
                }

                if (y >= 0) {
                    if ((mask == null) || (mask[y * w * 4 + x + 3] > 0)) {
                        if (hits == 0) {
                            newColors[4*y] = 0;
                            newColors[4*y + 1] = 0;
                            newColors[4*y + 2] = 0;
                            newColors[4*y + 3] = 0;
                        } else {
                            newColors[4*y] = Math.round(r / hits);
                            newColors[4*y + 1] = Math.round(g / hits);
                            newColors[4*y + 2] = Math.round(b / hits);
                            newColors[4*y + 3] = Math.round(a / hits);
                        }
                    } else {
                        newColors[4*y] = 0;
                        newColors[4*y + 1] = 0;
                        newColors[4*y + 2] = 0;
                        newColors[4*y + 3] = 0;
                    }
                }

                index += w * 4;
            }

            for (var y = 0; y < h; y++) {
                pixels[y * w * 4 + x] = newColors[4*y];
                pixels[y * w * 4 + x + 1] = newColors[4*y + 1];
                pixels[y * w * 4 + x + 2] = newColors[4*y + 2];
                pixels[y * w * 4 + x + 3] = newColors[4*y + 3];
            }
        }
    };


Filters.blur = function(canvas,ctx, hRadius, vRadius, iterations, mask){
  var imgData=ctx.getImageData(0,0,canvas.width,canvas.height);
  var data = imgData.data;
  Filters._premultiply(data);
  for (var i = 0; i < iterations; i++) {
    Filters._boxBlurHorizontal(data, mask, canvas.width, canvas.height, Math.floor(hRadius / 2));
    Filters._boxBlurVertical(data, mask, canvas.width, canvas.height, Math.floor(vRadius / 2));
  }
  
  Filters._unpremultiply(data);
  
  var width = canvas.width;
  var height = canvas.height;
  var retCanvas = Filters.createCanvas(width,height);
  var retImg = retCanvas.getContext("2d");
  retImg.putImageData(imgData,0,0);  
  return retCanvas;
}

Filters._moveRGB = function(width, height, rgb, deltaX, deltaY, fill) {
        var img = Filters.createCanvas(width,height);

        var ig=img.getContext("2d");                        
        
        Filters._setRGB(ig,0,0,width,height,rgb);
        var retImg = Filters.createCanvas(width,height);
        retImg.width = width;
        retImg.heigth = height;
        var g = retImg.getContext("2d");               
        g.fillStyle = fill;
        g.globalCompositeOperation = "copy";  
        g.fillRect(0, 0, width, height);                                      
        g.drawImage(img, deltaX,deltaY);
        return g.getImageData(0, 0, width, height).data;
    };


Filters.FULL = 1;
Filters.INNER = 2;
Filters.OUTER = 3;

Filters._setRGB = function(ctx, x, y, width, height, data){
    var id = ctx.createImageData(width,height);
    for(var i=0;i<data.length;i++){
        id.data[i] = data[i];
    }
    ctx.putImageData(id,x,y);
};

Filters.gradientGlow = function(srcCanvas,src, blurX, blurY, angle, distance, colors, ratios, type, iterations, strength, knockout) {
   var width = canvas.width;
   var height = canvas.height;
   var retCanvas = Filters.createCanvas(width,height);
   var retImg = retCanvas.getContext("2d");

   var gradCanvas = Filters.createCanvas(256,1);

   var gradient = gradCanvas.getContext("2d");
   var grd=ctx.createLinearGradient(0,0,255,0);
   for(var s=0;s<colors.length;s++){
       var v = "rgba("+colors[s][0]+","+colors[s][1]+","+colors[s][2]+","+colors[s][3]+")";
       grd.addColorStop(ratios[s],v);
   }
   gradient.fillStyle = grd;
   gradient.fillRect(0,0,256, 1);
   var gradientPixels = gradient.getImageData(0,0,gradCanvas.width,gradCanvas.height).data;

   var angleRad = angle / 180 * Math.PI;
   var moveX = (distance * Math.cos(angleRad));
   var moveY = (distance * Math.sin(angleRad));            
   var srcPixels = src.getImageData(0,0,width,height).data;
   var revPixels = [];
   for (var i = 0; i < srcPixels.length; i+=4) {
       revPixels[i] = srcPixels[i];
       revPixels[i+1] = srcPixels[i+1];
       revPixels[i+2] = srcPixels[i+2];
       revPixels[i+3] = 255-srcPixels[i+3];
   }            
   var shadow = [];
   for (var i = 0; i < srcPixels.length; i+=4) {
       var alpha = srcPixels[i+3];
       shadow[i] = 0;
       shadow[i+1] = 0;
       shadow[i+2] = 0;
       shadow[i+3] = Math.round(alpha * strength);
   }
   var colorAlpha = "rgba(0,0,0,0)";           
   shadow = Filters._moveRGB(width, height, shadow, moveX, moveY, colorAlpha);

   Filters._setRGB(retImg, 0, 0, width, height, shadow);

   var mask = null;
   if(type == Filters.INNER){
       mask = srcPixels;
   }
   if(type == Filters.OUTER){
       mask = revPixels;
   }


   retCanvas = Filters.blur(retCanvas,retCanvas.getContext("2d"), blurX, blurY, iterations,mask);
   retImg = retCanvas.getContext("2d");
   shadow = retImg.getImageData(0, 0, width, height).data;

   if(mask!=null){
      for (var i = 0; i < mask.length; i+=4) {
         if(mask[i+3] == 0){
            shadow[i] = 0;
            shadow[i+1] = 0;
            shadow[i+2] = 0;
            shadow[i+3] = 0;
         }
      }
   }





   for (var i = 0; i < shadow.length; i+=4) {
      var a = shadow[i+3];
      shadow[i] = gradientPixels[a*4];
      shadow[i+1] = gradientPixels[a*4+1];
      shadow[i+2] = gradientPixels[a*4+2];
      shadow[i+3] = gradientPixels[a*4+3];                
   }
   
   Filters._setRGB(retImg,0,0,width,height,shadow);

   if (!knockout) {
      retImg.globalCompositeOperation = "destination-over";
      retImg.drawImage(srcCanvas,0,0);            
   }

   return retCanvas;
};




Filters.dropShadow = function(canvas,src, blurX, blurY, angle, distance, color,inner, iterations, strength,knockout) {
    var width = canvas.width;
    var height = canvas.height;
    var srcPixels = src.getImageData(0, 0, width, height).data;
    var shadow = [];
    for (var i = 0; i < srcPixels.length; i+=4) {
        var alpha = srcPixels[i+3];
        if (inner) {
            alpha = 255 - alpha;
        }
        shadow[i] = color[0];
        shadow[i+1] = color[1];
        shadow[i+2] = color[2];        
        var sa = color[3] * alpha * strength;
        if(sa>255) sa = 255;
        shadow[i+3] = Math.round(sa);
    }
    var colorFirst = "#000000";
    var colorAlpha = "rgba(0,0,0,0)";           
    var angleRad = angle / 180 * Math.PI;
    var moveX = (distance * Math.cos(angleRad));
    var moveY = (distance * Math.sin(angleRad));            
    shadow = Filters._moveRGB(width, height, shadow, moveX, moveY, inner ? colorFirst : colorAlpha);
    
    
    var retCanvas = Filters.createCanvas(canvas.width,canvas.height);
    Filters._setRGB(retCanvas.getContext("2d"),0,0,width,height,shadow);
    if(blurX>0 || blurY > 0){
        retCanvas = Filters.blur(retCanvas,retCanvas.getContext("2d"), blurX, blurY, iterations,null);       
    }
    shadow = retCanvas.getContext("2d").getImageData(0,0,width,height).data;    
    
    var srcPixels = src.getImageData(0, 0, width, height).data;
    for (var i = 0; i < shadow.length; i+=4) {
        var mask = srcPixels[i+3];
        if (!inner) {
            mask = 255 - mask;
        }
        shadow[i + 3] = mask * shadow[i + 3] / 255;
    }
    Filters._setRGB(retCanvas.getContext("2d"),0,0,width,height,shadow);
    
    if (!knockout) {
        var g = retCanvas.getContext("2d");
        g.globalCompositeOperation = "destination-over";
        g.drawImage(canvas, 0, 0);
    }
    
    return retCanvas;
};

Filters._cut = function(a,min,max){
    if(a > max) a = max;
    if(a < min) a = min;
    return a;
}

Filters.gradientBevel = function(canvas, src, colors, ratios, blurX, blurY, strength, type, angle, distance, knockout, iterations) {
        var width = canvas.width;
        var height = canvas.height;
        var retImg = Filters.createCanvas(width,height);
        var srcPixels = src.getImageData(0, 0, width, height).data;

        var revPixels = [];
        for (var i = 0; i < srcPixels.length; i+=4) {
            revPixels[i] = srcPixels[i];
            revPixels[i+1] = srcPixels[i+1];
            revPixels[i+2] = srcPixels[i+2];
            revPixels[i+3] = 255-srcPixels[i+3];
        }    

        var gradient = Filters.createCanvas(512, 1);            
        var gg = gradient.getContext("2d");

        var grd=ctx.createLinearGradient(0,0,511,0);
        for(var s=0;s<colors.length;s++){                
            var v = "rgba("+colors[s][0]+","+colors[s][1]+","+colors[s][2]+","+colors[s][3]+")";   
            grd.addColorStop(ratios[s],v);
        }
        gg.fillStyle = grd;
        gg.globalCompositeOperation = "copy";
        gg.fillRect(0,0,gradient.width, gradient.height);
        var gradientPixels = gg.getImageData(0, 0, gradient.width, gradient.height).data;


        if(type!=Filters.OUTER){
            var hilightIm = Filters.dropShadow(canvas,src,0, 0, angle, distance, [255,0,0,1], true, iterations, strength, true);
            var shadowIm = Filters.dropShadow(canvas,src, 0, 0, angle + 180, distance, [0,0,255,1], true, iterations, strength, true);
            var h2 = Filters.createCanvas(width,height);
            var s2 = Filters.createCanvas(width,height);
            var hc=h2.getContext("2d");
            var sc=s2.getContext("2d");
            hc.drawImage(hilightIm,0,0);
            hc.globalCompositeOperation = "destination-out";
            hc.drawImage(shadowIm,0,0);

            sc.drawImage(shadowIm,0,0);
            sc.globalCompositeOperation = "destination-out";
            sc.drawImage(hilightIm,0,0);
            var shadowInner = s2;
            var hilightInner = h2;     
        }
        if(type!=Filters.INNER){
            var hilightIm = Filters.dropShadow(canvas,src,0, 0, angle + 180, distance, [255,0,0,1], false, iterations, strength, true);
            var shadowIm = Filters.dropShadow(canvas,src, 0, 0, angle, distance, [0,0,255,1], false, iterations, strength, true);
            var h2 = Filters.createCanvas(width,height);
            var s2 = Filters.createCanvas(width,height);
            var hc=h2.getContext("2d");
            var sc=s2.getContext("2d");
            hc.drawImage(hilightIm,0,0);
            hc.globalCompositeOperation = "destination-out";
            hc.drawImage(shadowIm,0,0);

            sc.drawImage(shadowIm,0,0);
            sc.globalCompositeOperation = "destination-out";
            sc.drawImage(hilightIm,0,0);
            var shadowOuter = s2;
            var hilightOuter = h2;
        }

        var hilightIm;
        var shadowIm;
        switch(type)
        {               
           case Filters.OUTER:
              hilightIm = hilightOuter;
              shadowIm = shadowOuter;
              break;
           case Filters.INNER:
              hilightIm = hilightInner;
              shadowIm = shadowInner;
              break;
           case Filters.FULL:
              hilightIm = hilightInner;
              shadowIm = shadowInner;
              var hc=hilightIm.getContext("2d");
              hc.globalCompositeOperation = "source-over";
              hc.drawImage(hilightOuter,0,0);
              var sc=shadowIm.getContext("2d");
              sc.globalCompositeOperation = "source-over";
              sc.drawImage(shadowOuter,0,0);
              break;
        }

        var mask = null;
        if(type == Filters.INNER){
            mask = srcPixels;
        }
        if(type == Filters.OUTER){
            mask = revPixels;
        }                                   

        var retc = retImg.getContext("2d");
        retc.fillStyle="#000000";
        retc.fillRect(0,0,width,height);
        retc.drawImage(shadowIm,0,0);
        retc.drawImage(hilightIm,0,0);

        retImg = Filters.blur(retImg,retImg.getContext("2d"),blurX,blurY,iterations,mask);
        var ret = retImg.getContext("2d").getImageData(0, 0, width, height).data;

        for (var i = 0; i < srcPixels.length; i+=4) {
            var ah = ret[i] * strength;
            var as = ret[i+2] * strength;
            var ra = Filters._cut(ah-as,-255,255);
            ret[i] = gradientPixels[4*(255 + ra)];
            ret[i+1] = gradientPixels[4*(255 + ra) + 1];
            ret[i+2] = gradientPixels[4*(255 + ra) + 2];
            ret[i+3] = gradientPixels[4*(255 + ra) + 3];                                                                               
        }
        Filters._setRGB(retImg.getContext("2d"), 0, 0, width, height, ret);                                                   


        if (!knockout) {
            var g = retImg.getContext("2d");
            g.globalCompositeOperation = "destination-over";
            g.drawImage(canvas, 0, 0);
        }
        return retImg;
}
Filters.bevel = function(canvas, src, blurX, blurY, strength, type, highlightColor, shadowColor, angle, distance, knockout, iterations) {
        return Filters.gradientBevel(canvas,src, [
            shadowColor,
            [shadowColor[0],shadowColor[1], shadowColor[2], 0], 
            [highlightColor[0], highlightColor[1], highlightColor[2], 0],                                   
            highlightColor
        ], [0, 127 / 255, 128 / 255, 1], blurX, blurY, strength, type, angle, distance, knockout, iterations);
    }




//http://www.html5rocks.com/en/tutorials/canvas/imagefilters/
Filters.convolution = function(canvas,ctx, weights, opaque) {
  var pixels = ctx.getImageData(0,0,canvas.width,canvas.height);
  var side = Math.round(Math.sqrt(weights.length));
  var halfSide = Math.floor(side/2);
  var src = pixels.data;
  var sw = pixels.width;
  var sh = pixels.height;
  // pad output by the convolution matrix
  var w = sw;
  var h = sh;
  var outCanvas = Filters.createCanvas(w,h);
  var outCtx = outCanvas.getContext("2d");
  var output = outCtx.getImageData(0,0,w,h);
  var dst = output.data;
  // go through the destination image pixels
  var alphaFac = opaque ? 1 : 0;
  for (var y=0; y<h; y++) {
    for (var x=0; x<w; x++) {
      var sy = y;
      var sx = x;
      var dstOff = (y*w+x)*4;
      // calculate the weighed sum of the source image pixels that
      // fall under the convolution matrix
      var r=0, g=0, b=0, a=0;
      for (var cy=0; cy<side; cy++) {
        for (var cx=0; cx<side; cx++) {
          var scy = sy + cy - halfSide;
          var scx = sx + cx - halfSide;
          if (scy >= 0 && scy < sh && scx >= 0 && scx < sw) {
            var srcOff = (scy*sw+scx)*4;
            var wt = weights[cy*side+cx];
            r += src[srcOff] * wt;
            g += src[srcOff+1] * wt;
            b += src[srcOff+2] * wt;
            a += src[srcOff+3] * wt;
          }
        }
      }
      dst[dstOff] = r;
      dst[dstOff+1] = g;
      dst[dstOff+2] = b;
      dst[dstOff+3] = a + alphaFac*(255-a);
    }
  }
  outCtx.putImageData(output,0,0);
  return outCanvas;
};

Filters.colorMatrix = function(canvas,ctx,m){
    var pixels = ctx.getImageData(0,0,canvas.width,canvas.height);

    var data=pixels.data;        
    for(var i=0;i<data.length;i+=4)
    {
            var r=i;
            var g=i+1;
            var b=i+2;
            var a=i+3;

            var oR=data[r];
            var oG=data[g];
            var oB=data[b];
            var oA=data[a];

            data[r] = (m[0]  * oR) + (m[1]  * oG) + (m[2]  * oB) + (m[3]  * oA) + m[4];
            data[g] = (m[5]  * oR) + (m[6]  * oG) + (m[7]  * oB) + (m[8]  * oA) + m[9];
            data[b] = (m[10] * oR) + (m[11] * oG) + (m[12] * oB) + (m[13] * oA) + m[14];
            data[a] = (m[15] * oR) + (m[16] * oG) + (m[17] * oB) + (m[18] * oA) + m[19];
    }
    var outCanvas = Filters.createCanvas(canvas.width,canvas.height);
    var outCtx = outCanvas.getContext("2d");
    outCtx.putImageData(pixels,0,0);
    return outCanvas;
};


Filters.glow = function(canvas,src,blurX,blurY,strength,color, inner,knockout,iterations) {
        return Filters.dropShadow(canvas,src, blurX, blurY, 45, 0, color, inner, iterations, strength, knockout);
};
