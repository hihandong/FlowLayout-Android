package han.anthony.flowlayout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import han.anthony.flowlayout.myLogger.L;

/**
 * Created by senior on 2016/10/24.
 */

public class FlowLayout extends ViewGroup {
    public FlowLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlowLayout(Context context) {
        this(context, null);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
       //int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        //子View是wrap_content时使用
        //当前最大的宽和高!!
        int width = 0;
        int height =getPaddingBottom()+getPaddingTop();

        //记录当前行的宽度和高度
        int lineWidth = 0;
        int lineHeight = 0;

        //得到子View的个数
        int cCount = getChildCount();

        for (int i = 0; i < cCount; i++) {
            View child = getChildAt(i);
            //测量子View的宽和高
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
            //得到LayoutParams
            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
            //子View占据的宽度和高度
            int childWidth = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            int childHeight = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;

            //换行的情况
            if (lineWidth + childWidth > widthSize-getPaddingLeft()-getPaddingRight()) {
                width = Math.max(width, childWidth);
                lineWidth = childWidth;
                /**
                 *把上一行的高度加到height
                 */
                height += lineHeight;
                lineHeight = childHeight;
            } else {
                //未换行
                lineWidth += childWidth;
                lineHeight = Math.max(lineHeight, childHeight);
            }
            //最后一个控件
            if (i == cCount - 1) {
                width = Math.max(width, lineWidth);
                height += lineHeight;
            }

            switch (widthMode) {
                case MeasureSpec.AT_MOST:
                    setMeasuredDimension(width, height);
                    break;
                case MeasureSpec.EXACTLY:
                    setMeasuredDimension(widthSize, heightSize);
                    break;
                default:
                    throw new RuntimeException("MeasureSpec.UNSPECIFIED暂时不支持!");
            }

        }


        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    //按行存储所有的子View
    private List<List<View>> mAllViews = new ArrayList<>();
    //每一行的高度
    private List<Integer> mLineHeights = new ArrayList<>();

    /**
     * 对子View设置位置
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mAllViews.clear();
        mLineHeights.clear();
        //当前FlowLayout的宽度
        int width = getWidth();
        int lineWidth = 0;
        int lineHeight = 0;
        List<View> lineViews = new ArrayList<>();
        int cCount = getChildCount();
        for (int i = 0; i < cCount; i++) {
            View child = getChildAt(i);
            if(child.getVisibility()==View.GONE){
                //
                continue;
            }
            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();

            //如果需要换行
            if (childWidth + lineWidth + lp.leftMargin + lp.rightMargin > width-getPaddingLeft()-getPaddingRight()) {
                //保存上一行的高度
                mLineHeights.add(lineHeight);
                //记录上一行的Views
                mAllViews.add(lineViews);

                //重置行宽和行高
                lineWidth = 0;
                lineHeight = childHeight + lp.topMargin + lp.bottomMargin;
                //重置lineViews集合
                lineViews = new ArrayList<>();
            }
            /**
             * 不需要换行
             */

            lineViews.add(child);
            lineWidth += childWidth + lp.leftMargin + lp.rightMargin;
            lineHeight = Math.max(lineHeight, childHeight + lp.topMargin + lp.bottomMargin);
        }//for end!
        //处理最后一行
        mAllViews.add(lineViews);
        mLineHeights.add(lineHeight);

        //设置子View的位置
        int left = getPaddingLeft();
        int top = getPaddingTop();

        //行数
        int lineNum = mAllViews.size();
        L.e("int lineNum=mAllViews.size();", lineNum);
        for (int i = 0; i < lineNum; i++) {
            //当前行所有的子View
            lineViews = mAllViews.get(i);
            lineHeight = mLineHeights.get(i);
            //设置当前行的子View
            for (int j = 0; j < lineViews.size(); j++) {
                View child = lineViews.get(j);
                MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
                int lc = left + lp.leftMargin;
                int tc = top + lp.topMargin;
                int rc = lc + child.getMeasuredWidth();
                int bc = tc + child.getMeasuredHeight();
                //为子View布局
                child.layout(lc, tc, rc, bc);
                L.e("设置子View:" + lc);

                left += child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            }
            left = getPaddingLeft();
            top += lineHeight;
        }
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        //因为流式布局只关心间距所以这里返回的是MarginLayoutParams
        return new MarginLayoutParams(getContext(), attrs);
    }
}
