
package com.mediatek.nfc.tag.record.param;

import android.os.Handler;
import android.view.View;

/**
 * General super class for each parameter tag
 * 
 * @author MTK80906
 */
public abstract class ParamItem {
    /**
     * Get the detail layout for editing this parameter
     * 
     * @return
     */
    public abstract View getEditItemView();

    /**
     * Get the detail layout for view this parameter from history
     * 
     * @return
     */
    public abstract View getHistoryItemView(String paramStr);

    /**
     * Get the detail layout when reading this parameter
     * 
     * @return
     */
    public abstract View getReadItemView(String paramStr);

    /**
     * Get a string stand for the selected parameter value
     * 
     * @return
     */
    public abstract String getParamStr();

    /**
     * Whether or not item can parse this parameter string
     * 
     * @param paramStr
     * @return
     */
    public abstract boolean match(String paramStr);

    /**
     * Make the item in page take effect. For SpinnerParamItem, judge which item
     * is selected, then call enableParam(Handler handler, String newStatus)
     * 
     * @param handler enable parameter result call back handler
     * @return
     */
    public abstract boolean enableParam(Handler handler);

    /**
     * Make the item in page take effect
     * 
     * @param handler enable parameter result call back handler
     * @param newStatus target status, like 0,1 ...
     * @return
     */
    public abstract boolean enableParam(Handler handler, String newStatus);

    /**
     * Get label for this parameter, and it can be shown to user as a
     * introduction of this item
     * 
     * @return
     */
    public abstract String getLabel();

    /**
     * Initialize layout view for this item, set item specific spinner value for
     * it
     * 
     * @return
     */
    public abstract View initLayoutView();

}
