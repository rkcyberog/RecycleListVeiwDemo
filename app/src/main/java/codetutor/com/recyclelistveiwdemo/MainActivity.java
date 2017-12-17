package codetutor.com.recyclelistveiwdemo;

import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements ListAdapterWithRecycleView.PersonModifier{


     @BindView(R.id.recycleListView) RecyclerView recyclerView;
    AppUtility appUtility;

    ListAdapterWithRecycleView listAdapterWithRecycleView;


     @BindView(R.id.editTextFirstName) EditText editTextFirstName;
     @BindView(R.id.editTextLastName) EditText editTextLastName;
     @BindView(R.id.spinnerNationality) Spinner spinnerNationality;
     @BindView(R.id.radioGroupGender) RadioGroup radioGroup;
     @BindView(R.id.buttonAdd) Button buttonAdd;

    List<Person> people;
    List<Object> catalogue;
    int modificationIndex=-1;

    String firstName, lastName, nationality;
    Person.GENDER gender;

    LinearLayoutManager linearLayoutManager;
    GridLayoutManager gridLayoutManager;
    StaggeredGridLayoutManager staggeredGridLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        appUtility=AppUtility.getAppUtility(getApplicationContext());

        initPersonInputForm();

        people = appUtility.getPeople();
        catalogue=appUtility.getCatalogue();

        listAdapterWithRecycleView=new ListAdapterWithRecycleView(this,catalogue);
        listAdapterWithRecycleView.setPersonModifier(this);

        linearLayoutManager=new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        gridLayoutManager = new GridLayoutManager(this,2,GridLayoutManager.VERTICAL,false);

        staggeredGridLayoutManager = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);

        LinearLayoutManager layoutManager=new LinearLayoutManager(this);

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(listAdapterWithRecycleView);

    }

    private void initPersonInputForm(){

        spinnerNationality.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,appUtility.getUniqueNationalitiesArray()));

        buttonAdd.setTag("Add");

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                switch (i){
                    case R.id.radioButtonMale: gender = Person.GENDER.MALE; break;
                    case R.id.radioButtonFemale: gender = Person.GENDER.FEMALE; break;
                    default: gender=null;
                }
            }
        });

        spinnerNationality.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                nationality = getResources().getStringArray(R.array.nationalities)[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firstName = editTextFirstName.getText().toString();
                lastName  = editTextLastName.getText().toString();
                Person person=null;

                if(isInputDataValid()) {
                    person = new Person(firstName, lastName, gender, nationality);
                }else{
                    Toast.makeText(MainActivity.this,"Input Invalid",Toast.LENGTH_LONG).show();
                }

                String behaviour = (String)buttonAdd.getTag();
                if(behaviour.equalsIgnoreCase("Add")){
                    if(person!=null){
                        people.add(person);
                        listAdapterWithRecycleView.notifyDataSetChanged();
                        recyclerView.scrollToPosition(people.size()-1);
                        clearInputForm();
                    }
                }else if(behaviour.equalsIgnoreCase("modify")){
                    if(person!=null){
                        try{
                            people.get(modificationIndex).setName(person.getName());
                            people.get(modificationIndex).setLastName(person.getLastName());
                            people.get(modificationIndex).setGender(person.getGender());
                            people.get(modificationIndex).setNationality((String)spinnerNationality.getSelectedItem());

                            listAdapterWithRecycleView.notifyItemChanged(modificationIndex);
                            clearInputForm();
                            buttonAdd.setTag("Add");
                            buttonAdd.setText("Add");
                        }catch (IndexOutOfBoundsException exception){
                            Toast.makeText(MainActivity.this,"Can't modify, item moved",Toast.LENGTH_LONG ).show();
                            listAdapterWithRecycleView.notifyDataSetChanged();
                            clearInputForm();
                            buttonAdd.setTag("Add");
                            buttonAdd.setText("Add");
                        }
                    }
                }

            }
        });
    }

    private boolean isInputDataValid(){
        if(AppUtility.isStringEmpty(firstName) || AppUtility.isStringEmpty(lastName) || AppUtility.isStringEmpty(nationality) || gender==null){
            return false;
        }else{
            return true;
        }
    }

    private void clearInputForm() {
        editTextFirstName.setText("");
        editTextLastName.setText("");
        radioGroup.clearCheck();
        spinnerNationality.setSelection(0);
    }

    @Override
    public void onPersonSelected(int position) {
        modificationIndex = position;
        Person person=people.get(position);
        buttonAdd.setTag("Modify");
        buttonAdd.setText("Modify");

        editTextFirstName.setText(person.getName());
        editTextLastName.setText(person.getLastName());
        if(person.getGender()== Person.GENDER.MALE){
            ((RadioButton)findViewById(R.id.radioButtonMale)).performClick();
        }else if(person.getGender()== Person.GENDER.FEMALE){
            ((RadioButton)findViewById(R.id.radioButtonFemale)).performClick();
        }
        spinnerNationality.setSelection(appUtility.getNationalityForSelectedIndex(person.getNationality()));
    }

    @Override
    public void onPersonDeleted(int position) {
        buttonAdd.setTag("Add");
        buttonAdd.setText("Add");
        clearInputForm();
    }

}
