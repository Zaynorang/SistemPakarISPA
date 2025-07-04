package com.example.sistempakarispa;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.sistempakarispa.R;
import com.example.sistempakarispa.ModelDiagnosa;

import java.util.ArrayList;


public class DiagnosaAdapter extends RecyclerView.Adapter<DiagnosaAdapter.KonsultasiHolder> {

    int varGlobal = 0;
    private Context ctx;
    private ArrayList<ModelDiagnosa> modelDiagnosaArrayList;

    public DiagnosaAdapter(Context context, ArrayList<ModelDiagnosa> items) {
        this.ctx = context;
        this.modelDiagnosaArrayList = new ArrayList<>();
        this.modelDiagnosaArrayList.addAll(items);
    }

    @Override
    public DiagnosaAdapter.KonsultasiHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_gejala, parent, false);
        return new DiagnosaAdapter.KonsultasiHolder(view);
    }

    @Override
    public void onBindViewHolder(KonsultasiHolder holder, final int position) {
        final ModelDiagnosa data = modelDiagnosaArrayList.get(position);

        holder.cbGejala.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton checkboxView, boolean isChecked) {
                ModelDiagnosa modelDiagnosa = (ModelDiagnosa) checkboxView.getTag();

                if (isChecked) {
                    varGlobal++;
                } else if (!isChecked) {
                    varGlobal--;
                }

                if (varGlobal >= 4) {
                    Toast.makeText(ctx, "Maksimal 3 pilihan saja!", Toast.LENGTH_LONG).show();
                    checkboxView.setChecked(false);
                    varGlobal--;
                } else {
                    modelDiagnosa.setSelected(isChecked);
                }
            }
        });

        holder.cbGejala.setText(data.getStrGejala());
        holder.cbGejala.setChecked(data.isSelected());
        holder.cbGejala.setTag(data);
    }

    @Override
    public int getItemCount() {
        return modelDiagnosaArrayList.size();
    }

    static class KonsultasiHolder extends RecyclerView.ViewHolder {
        CheckBox cbGejala;

        public KonsultasiHolder(View itemView) {
            super(itemView);
            cbGejala = itemView.findViewById(R.id.cbGejala);
        }
    }

}