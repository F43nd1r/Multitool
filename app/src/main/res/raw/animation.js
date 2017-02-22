var container = getEvent().getContainer();
var animations = [bulldoze, card, flip, shrink];
var tag = container.getTag("animation");
if(tag == null) return;
var config = JSON.parse(tag);
if(config == null || config.animation < 0) return;
var items = container.getAllItems();
var cWidth = container.getWidth();
var cHeight = container.getHeight();
var posX = container.getPositionX();
var posY = container.getPositionY();
var cPageX = Math.floor(posX /cWidth);
var cPageY = Math.floor(posY /cHeight);
var percentX = (posX - cPageX * cWidth) / cWidth;
var percentY = (posY - cPageY * cHeight) / cHeight;
for(var i = 0; i < items.length; i++){
	var item = items[i];
	var cent = center(item);
	var pageX = Math.floor(cent.x / cWidth);
	var pageY = Math.floor(cent.y / cHeight);
	var onPageX = pageX == cPageX;
	var onPageY = pageY == cPageY;
	if((onPageX || pageX == cPageX + 1) && (onPageY || pageY == cPageY +1)){
	    var animation = animations[config.animation](onPage(cent), onPageX, onPageY);
	    var pinMode = item.getProperties().getString("i.pinMode");
	    var transformX = pinMode.indexOf("X") == -1;
	    var transformY = pinMode.indexOf("Y") == -1;
	    if(transformX || transformY){
		    var a = item.getRootView().animate().setDuration(0).alpha(animation.alpha);
		    if(transformX) a.scaleX(animation.scale.x).translationX(animation.translation.x);
		    if(transformY) a.scaleY(animation.scale.y).translationY(animation.translation.y);
			a.start();
		}
	}
}

function center(item){
	var r = item.getRotation();
	r=r*Math.PI/180;
	var sin=Math.abs(Math.sin(r));
	var cos=Math.abs(Math.cos(r));
	var w=item.getWidth()*item.getScaleX();
	var h=item.getHeight()*item.getScaleY();
	return {x:item.getPositionX()+(w*cos+h*sin)*0.5,y:item.getPositionY()+(h*cos+w*sin)*0.5};﻿
}﻿

function onPage(cent){
    cent.x = positiveModulo(cent.x,cWidth);
    cent.y = positiveModulo(cent.y, cHeight);
    return cent;
}

function positiveModulo(i, mod){
    var result = i % mod;
    if(result<0) result+=mod;
    return result;
}

function getDefault(){
    return  {
        scale:{
            x:1,
            y:1
        },
        translation:{
            x:0,
            y:0
        },
        alpha:1
    }
}

function bulldoze(cent, isLeft, isTop){
    var result = getDefault();
    result.scale.x = isLeft ? 1 - percentX : percentX;
    result.scale.y = isTop ? 1 - percentY : percentY;
    result.translation.x = (isLeft ? (cWidth - cent.x) * percentX : cent.x * (percentX - 1));
    result.translation.y = (isTop ? (cHeight - cent.y) * percentY : cent.y * (percentY - 1));
    return result;
}

function card(cent, isLeft, isTop){
    var result = getDefault();
    if(!isLeft && isTop){
        result.translation.x = cWidth * (percentX - 1);
        result.alpha = percentX;
    } else if(isLeft && !isTop){
        result.translation.y = cHeight * (percentY - 1);
        result.alpha = percentY;
    }
    return result;
}

function flip(cent, isLeft, isTop){
    var result = getDefault();
    if(isLeft != percentX >= 0.5){
        result.scale.x = isLeft ? 1 - percentX * 2 : percentX * 2 - 1;
        result.translation.x = isLeft ?  2 * percentX * (cWidth - cent.x) :  2 * cent.x * (percentX - 1);
    } else  {
        result.alpha = 0;
    }
    if(isTop != percentY >= 0.5){
        result.scale.y = isTop ? 1 - percentY * 2 : percentY * 2 - 1;
        result.translation.y = isTop ?  2 * percentY * (cHeight - cent.y) :  2 * cent.y * (percentY - 1);
    } else{
        result.alpha = 0;
    }
    return result;
}

function shrink(cent, isLeft, isTop){
    var result = getDefault();
    if(Math.abs(percentX-0.5)<=Math.abs(percentY-0.5)){
        result.scale.x = result.scale.y = isLeft ? 1 - percentX * 0.75 : 0.25 + percentX * 0.75;
        result.translation.x = isLeft ? (cWidth - cent.x) * percentX * 0.75 : (cWidth - cent.x) * (0.75 - percentX * 0.75);
        result.translation.y = isLeft ? (cHeight/2 - cent.y) * percentX * 0.75 : (cHeight/2 - cent.y) * (0.75 - percentX * 0.75);
    }else{
        result.scale.x = result.scale.y = isTop ? 1 - percentY * 0.75 : 0.25 + percentY * 0.75;
        result.translation.x = isTop ? (cWidth/2 - cent.x) * percentY * 0.75 : (cWidth/2 - cent.x) * (0.75 - percentY * 0.75);
        result.translation.y = isTop ? (cHeight - cent.y) * percentY * 0.75 : (cHeight - cent.y) * (0.75 - percentY * 0.75);
    }
    return result;
}