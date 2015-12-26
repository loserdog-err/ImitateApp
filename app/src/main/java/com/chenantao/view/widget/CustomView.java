package com.chenantao.view.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by chenantao on 2015/12/25.
 */
public class CustomView extends View
{
	public CustomView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		setMeasuredDimension(300, 300);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	@Override
	protected void onDraw(Canvas canvas)
	{
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(Color.BLUE);
		paint.setStyle(Paint.Style.FILL);
		Path path = new Path();
		path.moveTo(0, 0);
		path.lineTo(300, 0);
		path.lineTo(300, 200);
		path.quadTo(150, 300, 0, 200);
		path.close();
		canvas.drawPath(path, paint);
	}
}
