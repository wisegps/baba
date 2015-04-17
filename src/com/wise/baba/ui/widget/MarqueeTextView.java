/**
 * 
 */
package com.wise.baba.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewDebug.ExportedProperty;
import android.widget.TextView;

/**
 *
 * @author c
 * @desc   baba
 * @date   2015-4-17
 *
 */
public class MarqueeTextView extends TextView {

	/**
	 * @param context
	 * @param attrs
	 */
	public MarqueeTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	@ExportedProperty(category = "focus")
	public boolean isFocused() {
		return true;
	}

}
