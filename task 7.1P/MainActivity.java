package com.example.lostfound;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    public static PostsAdapter postsAdapter;
    private OrmliteHelper ormliteHelper;
    public static List<PostDTO> postlist = new ArrayList<>();
    private ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ormliteHelper  = new OrmliteHelper(this);
        imageView      = findViewById(R.id.imageView4);
        boolean isLost = getIntent().getBooleanExtra("isLost", true);

        recyclerView = findViewById(R.id.postlist);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CreateOrView.class);;
                startActivity(intent);
            }
        });

        try {
            postlist = ormliteHelper.getDataByField(PostDTO.class, "isLost", isLost);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        postsAdapter = new PostsAdapter(this,postlist);
        recyclerView.setAdapter(postsAdapter);
        postsAdapter.notifyDataSetChanged();

    }

}


//create.java

package com.example.lostfound;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Create extends AppCompatActivity {

    private RadioButton lost,found;
    private EditText nameEditText, contactEditText, descriptionEditText, locationEditText;
    private TextView dateEditText;
    private Button saveButton;
    private Calendar calendar;
    int position = -1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        // Initialize calendar instance
        calendar = Calendar.getInstance();

        // Initialize views
        nameEditText = findViewById(R.id.name);
        contactEditText = findViewById(R.id.contact);
        descriptionEditText = findViewById(R.id.description);
        dateEditText = findViewById(R.id.date);
        locationEditText = findViewById(R.id.location);
        saveButton = findViewById(R.id.save);
        lost = findViewById(R.id.lost);
        found = findViewById(R.id.found);

        dateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePicker();
            }
        });
        lost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                position = 0;
                found.setChecked(false);
            }
        });
        found.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                position = 1;
                lost.setChecked(false);
            }
        });

        // Save button click listener
        saveButton.setOnClickListener(view -> {
            try {
                savePost();
                navigateToAnotherActivity(CreateOrView.class);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
    private void showDatePicker() {
        // Create DatePickerDialog and set initial date to current date
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, dateSetListener,
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    // DatePickerDialog.OnDateSetListener to handle the selected date
    private DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            // Update calendar instance with selected date
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            // Update EditText text with selected date
            updateDateEditText();
        }
        private void updateDateEditText() {
            String myFormat = "dd/MM/yyyy"; // Date format
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());
            dateEditText.setText(sdf.format(calendar.getTime()));
        }
    };
    private void savePost() throws SQLException {

        OrmliteHelper ormliteHelper = new OrmliteHelper(this);
        PostDTO postDTO             = new PostDTO();

        // Get input values
        String name = nameEditText.getText().toString();
        String contact = contactEditText.getText().toString();
        String description = descriptionEditText.getText().toString();
        String date = dateEditText.getText().toString();
        String location = locationEditText.getText().toString();


        // Perform validation
        if (name.isEmpty() || contact.isEmpty() || description.isEmpty() || date.isEmpty() || location.isEmpty() || position == -1) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        postDTO.setName(name);
        postDTO.setContact(contact);
        postDTO.setDescription(description);
        postDTO.setLocation(location);
        postDTO.setDate(date);
        if(position == 0) {
            postDTO.setLost(true);
        } else if (position == 1) {
            postDTO.setLost(false);
        }
        ormliteHelper.createOrUpdate(postDTO);
        Toast.makeText(this, "Post Successfully Added", Toast.LENGTH_SHORT).show();
    }
    private void navigateToAnotherActivity(Class<?> destinationActivity) {
        Intent intent = new Intent(this, destinationActivity);
        startActivity(intent);
    }
}


//CreateOrView.java



package com.example.lostfound;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

public class CreateOrView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_or_view);

        Button createButton = findViewById(R.id.create);
        Button showButton = findViewById(R.id.show);
        ImageView imageView = findViewById(R.id.imageView);

        // Load the GIF with Glide
        Glide.with(this)
                .load(R.drawable.newpost)
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.RESOURCE)) // Cache the GIF resource
                .into(imageView);

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigateToAnotherActivity(Create.class);
            }
        });
        showButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigateToAnotherActivity(com.example.lostfound.View.class);
            }
        });
    }
    private void navigateToAnotherActivity(Class<?> destinationActivity) {
        Intent intent = new Intent(this, destinationActivity);
        startActivity(intent);
    }
}

//OrmliteHelper.java


package com.example.lostfound;


import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.util.List;

public class OrmliteHelper extends OrmLiteSqliteOpenHelper {

    public static final String DB_NAME = "post_.db";
    private static final int DB_VERSION = 1;
    private Context context;
    // Public methods
    public OrmliteHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
       getWritableDatabase();
    }
    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, PostDTO.class);
        } catch (SQLException | java.sql.SQLException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {

    }
    public <T> List getAll(Class clazz) throws SQLException, java.sql.SQLException {
        Dao<T, ?> dao = getDao(clazz);
        return dao.queryForAll();
    }
    public <T> Dao.CreateOrUpdateStatus createOrUpdate(T obj) throws SQLException, java.sql.SQLException {
        Dao<T, ?> dao = (Dao<T, ?>) getDao(obj.getClass());
        return dao.createOrUpdate(obj);
    }
    public <T> Dao<T, ?> getDaoFor(Class<T> clazz) throws SQLException, java.sql.SQLException {
        return getDao(clazz);
    }
    public <T> void deleteById(Class<T> clazz, int id) throws SQLException, java.sql.SQLException {
        Dao<T, Integer> dao = (Dao<T, Integer>) getDaoFor(clazz);
        dao.deleteById(id);
    }
    public <T> void update(Class<T> clazz, T obj) throws SQLException, java.sql.SQLException {
        Dao<T, Integer> dao = (Dao<T, Integer>) getDaoFor(clazz);
        dao.update(obj);
    }
    public <T> List<T> getDataByField(Class<T> clazz, String fieldName, Object value) throws SQLException, java.sql.SQLException {
        Dao<T, ?> dao = getDao(clazz);
        QueryBuilder<T, ?> queryBuilder = dao.queryBuilder();
        queryBuilder.where().eq(fieldName, value);
        return queryBuilder.query();
    }


}


//PostDTO.java


package com.example.lostfound;

import com.j256.ormlite.field.DatabaseField;

public class PostDTO {
    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField
    private String postType;

    @DatabaseField
    private String name;

    @DatabaseField
    private String contact;

    @DatabaseField
    private String description;

    @DatabaseField
    private String date;

    @DatabaseField
    private String location;

    @DatabaseField
    private boolean isLost;

    public PostDTO() {
        // Empty constructor needed by ORM
    }

    public PostDTO(String postType, String name, String contact, String description, String date, String location, boolean isLost) {
        this.postType = postType;
        this.name = name;
        this.contact = contact;
        this.description = description;
        this.date = date;
        this.location = location;
        this.isLost = isLost;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPostType() {
        return postType;
    }
    public boolean isLost() {
        return isLost;
    }

    public void setLost(boolean lost) {
        isLost = lost;
    }
    public void setPostType(String postType) {
        this.postType = postType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}


//PostsAdapter.java


package com.example.lostfound;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lostfound.PostDTO;
import com.example.lostfound.R;

import java.sql.SQLException;
import java.util.List;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ItemViewHolder> {

    private Context context;
    private List<PostDTO> postlist;
    private OrmliteHelper ormliteHelper;

    public PostsAdapter(Context context, List<PostDTO> postlist) {
        this.context = context;
        this.postlist = postlist;
    }

    @NonNull
    @Override
    public PostsAdapter.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostsAdapter.ItemViewHolder holder, int position) {
        PostDTO postDTO = postlist.get(position);
        if(!postDTO.isLost()){
            holder.title.setText("Found");
        }
        holder.description.setText(postDTO.getDescription());
        holder.date.setText(postDTO.getDate());
        holder.location.setText(postDTO.getLocation());
        holder.contact.setText(postDTO.getContact());

        holder.deleteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Are you sure you want to delete this Post?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                ormliteHelper = new OrmliteHelper(context);
                                try {
                                    ormliteHelper.deleteById(PostDTO.class , postlist.get(position).getId());
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                                RefreshItems(postDTO.isLost());
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User clicked No button, do nothing
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();

            }
        });
    }
    private void RefreshItems(boolean isLost) {
        try {
            MainActivity.postlist.clear(); // Clear the existing list
            postlist = ormliteHelper.getDataByField(PostDTO.class, "isLost", isLost);// Add all items from the database
            notifyDataSetChanged(); // Notify adapter that data has changed
        } catch (SQLException e) {
            Toast.makeText(context, "Can not Load Data", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return postlist.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {

        private TextView title;
        private TextView description,date,location,contact;
        private ImageView deleteIcon;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.lof);
            description = itemView.findViewById(R.id.des);
            deleteIcon = itemView.findViewById(R.id.delete);
            date = itemView.findViewById(R.id.date);
            location = itemView.findViewById(R.id.location);
            contact = itemView.findViewById(R.id.number);
        }
    }
}



//view.java


package com.example.lostfound;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

public class View extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);

        // Initialize views
        LinearLayout lostListLayout = findViewById(R.id.lost_list);
        LinearLayout foundListLayout = findViewById(R.id.found_list);

        ImageView imageView = findViewById(R.id.imageView2);

        // Load the GIF with Glide
        Glide.with(this)
                .load(R.drawable.post)
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.RESOURCE)) // Cache the GIF resource
                .into(imageView);

        lostListLayout.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View view) {
                navigateToAnotherActivity(MainActivity.class, true);
            }
        });
        foundListLayout.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View view) {
                navigateToAnotherActivity(MainActivity.class, false);
            }
        });

    }
    private void navigateToAnotherActivity(Class<?> destinationActivity, boolean isLost) {
        Intent intent = new Intent(this, destinationActivity);
        intent.putExtra("isLost", isLost);
        startActivity(intent);
    }

}