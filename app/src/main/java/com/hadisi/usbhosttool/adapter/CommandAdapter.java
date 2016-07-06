package com.hadisi.usbhosttool.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hadisi.usbhosttool.R;


/**
 * Created by wugang on 2015/11/12.
 */
public class CommandAdapter extends RecyclerView.Adapter<CommandAdapter.CommandViewHolder> {

    private Context mContext;
    private String[] mCommands;
    private LayoutInflater mInflater;

    private OnItemClickListener mOnItemClickListener;

    private int mSelectPosition = -1;

    public interface OnItemClickListener{
        void onItemClick(View view, int postion);
        void onItemLongClick(View view, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        mOnItemClickListener = listener;
    }


    public CommandAdapter(Context context, String[] Commands){
        mContext = context;
        mCommands = Commands;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public CommandViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.command_textview,parent,false);
        CommandViewHolder viewHolder = new CommandViewHolder(itemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final CommandViewHolder holder, final int position) {
        holder.Command.setText(mCommands[position]);
        if(mSelectPosition == position){
            holder.Command.setTextColor(mContext.getResources().getColor(R.color.textSelectColor));
        } else {
            holder.Command.setTextColor(mContext.getResources().getColor(R.color.textColor));
        }
        if(mOnItemClickListener != null){
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int layoutPostition = holder.getLayoutPosition();
                    mOnItemClickListener.onItemClick(v, layoutPostition);
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int layoutPostition = holder.getLayoutPosition();
                    mOnItemClickListener.onItemLongClick(v, layoutPostition);
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mCommands.length;
    }

    public void setSelectPosition(int position){
        mSelectPosition = position;
    }
    
    class CommandViewHolder extends RecyclerView.ViewHolder{

        TextView Command;
        public CommandViewHolder(View itemView) {
            super(itemView);
            Command = (TextView) itemView.findViewById(R.id.commandItem);
        }
    }
}
