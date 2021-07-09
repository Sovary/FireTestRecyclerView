package com.hellokh.sovary.firetest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class RVActivity extends AppCompatActivity
{
    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView recyclerView;
    DAOEmployee dao;
    FirebaseRecyclerAdapter adapter ;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rv);
        swipeRefreshLayout = findViewById(R.id.swip);
        recyclerView = findViewById(R.id.rv);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        dao = new DAOEmployee();
        FirebaseRecyclerOptions<Employee> option =
                new FirebaseRecyclerOptions.Builder<Employee>()
                .setQuery(dao.get(), new SnapshotParser<Employee>()
                {
                    @NonNull
                    @Override
                    public Employee parseSnapshot(@NonNull DataSnapshot snapshot)
                    {
                        Employee emp = snapshot.getValue(Employee.class);
                        emp.setKey(snapshot.getKey());
                        return emp;
                    }
                }).build();
        adapter = new FirebaseRecyclerAdapter(option)
        {
            @Override
            protected void onBindViewHolder( RecyclerView.ViewHolder viewHolder, int i,Object o)
            {
                EmployeeVH vh = (EmployeeVH) viewHolder;
                Employee emp = (Employee)o;
                vh.txt_name.setText(emp.getName());
                vh.txt_position.setText(emp.getPosition());
                vh.txt_option.setOnClickListener(v->
                {
                    PopupMenu popupMenu =new PopupMenu(RVActivity.this,vh.txt_option);
                    popupMenu.inflate(R.menu.option_menu);
                    popupMenu.setOnMenuItemClickListener(item->
                    {
                        switch (item.getItemId())
                        {
                            case R.id.menu_edit:
                                Intent intent=new Intent(RVActivity.this,MainActivity.class);
                                intent.putExtra("EDIT",emp);
                                startActivity(intent);
                                break;
                            case R.id.menu_remove:
                                DAOEmployee dao=new DAOEmployee();
                                dao.remove(emp.getKey()).addOnSuccessListener(suc->
                                {
                                    Toast.makeText(RVActivity.this, "Record is removed", Toast.LENGTH_SHORT).show();
                                }).addOnFailureListener(er->
                                {
                                    Toast.makeText(RVActivity.this, ""+er.getMessage(), Toast.LENGTH_SHORT).show();
                                });

                                break;
                        }
                        return false;
                    });
                    popupMenu.show();
                });
            }

            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(RVActivity.this).inflate(R.layout.layout_item,parent,false);
                return new EmployeeVH(view);
            }

            @Override
            public void onDataChanged()
            {
                Toast.makeText(RVActivity.this, "Data Changed", Toast.LENGTH_SHORT).show();
            }
        };
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        adapter.stopListening();
    }
}
