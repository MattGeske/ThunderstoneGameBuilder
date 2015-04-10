package com.mgeske.tsgamebuilder;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.LinearLayout;

public class CheckableLinearLayout extends LinearLayout implements Checkable {
	//inspired by http://www.marvinlabs.com/2010/10/29/custom-listview-ability-check-items/
	private boolean isChecked = false;
	private List<Checkable> checkableChildren = new ArrayList<Checkable>();

	public CheckableLinearLayout(Context context) {
		super(context);
	}

	public CheckableLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		addCheckableChildren(this);
	}
	
	private void addCheckableChildren(ViewGroup viewGroup) {
		int childCount = viewGroup.getChildCount();
		for(int i = 0; i < childCount; i++) {
			View child = viewGroup.getChildAt(i);
			if(child instanceof Checkable) {
				checkableChildren.add((Checkable)child);
			} else if(child instanceof ViewGroup) {
				addCheckableChildren((ViewGroup)child);
			}
		}
	}

	@Override
	public void setChecked(boolean checked) {
		isChecked = checked;
		updateChildren();
	}

	@Override
	public boolean isChecked() {
		return isChecked;
	}

	@Override
	public void toggle() {
		isChecked = !isChecked;
		updateChildren();
	}
	
	private void updateChildren() {
		for(Checkable child : checkableChildren) {
			child.setChecked(isChecked);
		}
	}

}
