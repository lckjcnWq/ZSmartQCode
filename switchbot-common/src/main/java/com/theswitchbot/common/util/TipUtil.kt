package com.theswitchbot.common.util

import android.animation.ObjectAnimator
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Property
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.qmuiteam.qmui.util.QMUIKeyboardHelper
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheetRootLayout
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog
import com.theswitchbot.common.CommonApp
import com.theswitchbot.common.R
import com.theswitchbot.common.widget.LoadingDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object TipUtil {
    fun showToast(context:Context,message:String,duration:Int=Toast.LENGTH_SHORT){
        Toast.makeText(context,message,duration).show()
    }

    fun showToast(context:Context,resId:Int,duration:Int=Toast.LENGTH_SHORT){
        Toast.makeText(context,context.getText(resId),duration).show()
    }

    fun showDialog(context: Activity,message:String,iconType:Int=QMUITipDialog.Builder.ICON_TYPE_INFO){
        val dialog=QMUITipDialog.Builder(context).setTipWord(message).setIconType(iconType).create()
        dialog.show()
        GlobalScope.launch(Dispatchers.Main) {
            delay(1500)
            dialog.dismiss()
        }

    }

    fun showConfirmDialog(context: Activity,
                          message:String,
                          title:String?=null,
                          positiveText:Int=R.string.dialog_yes,
                          listener:()->Unit ={}){
        val builder=MaterialDialog.Builder(context).content(message).positiveText(positiveText).onPositive { dialog, which ->
            dialog.dismiss()
            listener.invoke()
        }
        title?.apply { builder.title(this) }
        builder.build().show()
    }

    fun showConfirmDialog(context: Activity,message:String,positiveText:Int=R.string.ok_ok,negativeText:Int=R.string.dialog_cancel,okListener:()->Unit ={},cancelListener:()->Unit={}){
        MaterialDialog.Builder(context).content(message)
            .positiveText(positiveText).negativeText(negativeText)
            .onPositive { dialog, which ->
                dialog.dismiss()
                okListener.invoke()
            }.onNegative{dialog, which ->
                dialog.dismiss()
                cancelListener.invoke()
            }.build().show()

    }

    //显示提示框：包括title，message，ok，cancle
    fun showConfirmDialog(context: Activity,title:String,message:String,
                          okStr:Int = 0,cancleStr:Int = 0,okListener:()->Unit ={},cancelListener:()->Unit={}) : Dialog {
        var okStrShow = R.string.dialog_yes
        if (okStr != 0){
            okStrShow = okStr
        }
        var cancleStrShow = R.string.dialog_cancel
        if (cancleStr != 0){
            cancleStrShow = cancleStr
        }

        val dialog=MaterialDialog.Builder(context).content(message).title(title).cancelable(false).canceledOnTouchOutside(false)
            .positiveText(okStrShow).negativeText(cancleStrShow)
            .onPositive { dialog, which ->
                dialog.dismiss()
                okListener.invoke()
            }.onNegative{dialog, which ->
                dialog.dismiss()
                cancelListener.invoke()
            }.build()
        dialog.show()

        return dialog
    }



    fun showDialog(context: Activity,resId:Int,iconType:Int=QMUITipDialog.Builder.ICON_TYPE_INFO){
        showDialog(context, context.resources.getString(resId), iconType)
    }

    fun showLoading(context: Activity,resId:Int,iconType:Int=QMUITipDialog.Builder.ICON_TYPE_LOADING):QMUITipDialog{
        val dialog=QMUITipDialog.Builder(context).setTipWord(context.resources.getString(resId)).setIconType(iconType).create()
        dialog.show()
        return dialog
    }

    /**
     * 不带图标
     */
    fun showBottomList(context:Context,title: String,data:List<String>,checkedIndex:Int?=null,gravityCenter:Boolean=false,onCheckedListener:(Int)->Unit={},onDismiss:()->Unit={}){
        showBottomList(context, title,null, data, checkedIndex, gravityCenter, onCheckedListener, onDismiss)
    }

    /**
     * 带图标
     */
    fun showBottomList(context: Context, title: String,iconList:List<Int>?=null,data:List<String>, checkedIndex:Int?=null, gravityCenter:Boolean=false, onCheckedListener:(Int)->Unit={}, onDismiss:()->Unit={}){
        var checked=false
        val builder= QMUIBottomSheet.BottomListSheetBuilder(context,true).setTitle(title).setNeedRightMark(false)
        data.forEachIndexed {index,it->
            if (iconList!=null&&iconList.size==data.size){
                builder.addItem(iconList[index],it,it)
            }else{
                builder.addItem(it)
            }
        }
        if (checkedIndex!=null){
            builder.setCheckedIndex(checkedIndex)
            builder.setNeedRightMark(true)
        }
        builder.setGravityCenter(gravityCenter)
        builder.setOnBottomDialogDismissListener{if(!checked)onDismiss.invoke()}
        builder.setOnSheetItemClickListener { dialog, _, position, _ ->
            checked=true
            onCheckedListener.invoke(position)
            dialog.dismiss()
        }.build().show()
    }


    fun formatException(exception: Throwable): String {
        var formattedString = "Internal Error"
        val temp = exception.message
        if (temp != null && temp.isNotEmpty()) {
            formattedString = temp.split("\\(").toTypedArray()[0]
            if (temp.isNotEmpty()) {
                return when (formattedString) {
                    "Confirmation code entered is not correct." -> {
                        CommonApp.instance.getString(R.string.incorrect_verification_code)
                    }
                    "Number of allowed operation has exceeded." -> {
                        CommonApp.instance.getString(R.string.request_code_too_frequently)
                    }
                    else -> {
                        formattedString
                    }
                }
            }
        }
        return formattedString
    }

    fun showNfcToast(context: Context, message: String?, backColor: String?) {
        val toastview: View = LayoutInflater.from(context).inflate(R.layout.nfc_toast_custom, null)
        val text = toastview.findViewById<View>(R.id.tv_message) as TextView
        text.setBackgroundColor(Color.parseColor(backColor))
        val para1 = text.layoutParams as LinearLayoutCompat.LayoutParams
        para1.height = dip2px(context,33f)
        para1.width = context.resources.displayMetrics.widthPixels
        text.layoutParams = para1
        text.text = message //要提示的文本
        val toast = Toast(context) //上下文
        toast.setGravity(Gravity.TOP, 0, 0) //位置居中
        toast.duration = Toast.LENGTH_LONG //设置短暂提示
        toast.view = toastview //把定义好的View布局设置到Toast里面
        toast.show()
    }

    /**
     * @Description 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    fun dip2px(context: Context,dpValue: Float): Int {
        val scale: Float = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }


    private var loadingDialog: LoadingDialog? = null
    fun showLoadingDialog(mContext: Context, isCancelAble:Boolean= false) {
        try {
            dismissLoadingDialog()
            loadingDialog = LoadingDialog(mContext)
            loadingDialog!!.setCancelable(isCancelAble)
            loadingDialog!!.setMessage(mContext.getString(R.string.text_loading))
            loadingDialog!!.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setLoadingMessage(message:String) {
        try {
            if (loadingDialog != null) {
                loadingDialog!!.setMessage(message)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun dismissLoadingDialog() {
        try {
            if (loadingDialog != null) {
                loadingDialog!!.dismiss()
                loadingDialog = null
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
}