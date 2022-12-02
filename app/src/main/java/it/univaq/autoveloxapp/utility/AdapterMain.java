package it.univaq.autoveloxapp.utility;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import it.univaq.autoveloxapp.R;
import it.univaq.autoveloxapp.model.Autovelox;

public class AdapterMain extends RecyclerView.Adapter<AdapterMain.ViewHolder> {
    private List<Autovelox> data = new ArrayList<Autovelox>();
    private OnAutoveloxAdapterListener listener;

    public AdapterMain(List<Autovelox> data){
        if(data != null){
            this.data = data;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.adapter_main,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.municipality_TextView.setText(this.data.get(position).getMunicipality());
        holder.region_TextView.setText(this.data.get(position).getRegion());
        holder.date_time_TextView.setText(this.data.get(position).getInsertion_date_time());
    }

    @Override
    public int getItemCount() {
        return this.data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView municipality_TextView, region_TextView, date_time_TextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            municipality_TextView = itemView.findViewById(R.id.textView_municipality);
            region_TextView = itemView.findViewById(R.id.textView_region);
            date_time_TextView = itemView.findViewById(R.id.textView_date_time);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(listener != null){
                        listener.OnOpenAutovelox(data.get(getAdapterPosition()));
                    }
                }
            });
        }
    }

    public interface OnAutoveloxAdapterListener{
        void OnOpenAutovelox(Autovelox autovelox);
    }

    public void setOnAutoveloxAdapterListener(OnAutoveloxAdapterListener listener){
        this.listener = listener;
    }
}
