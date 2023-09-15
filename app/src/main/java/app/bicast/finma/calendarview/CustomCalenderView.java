package app.bicast.finma.calendarview;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import app.bicast.finma.R;

public class CustomCalenderView extends LinearLayout{
    private static final String TAG = CustomCalenderView.class.getSimpleName();
    private ImageView previousButton, nextButton;
    private TextView currentDate;
    public GridView calendarGridView;
    private Button addEventButton;
    private static final int MAX_CALENDAR_COLUMN = 42;
    private int month, year;
    private SimpleDateFormat formatter = new SimpleDateFormat("MMMM yyyy", Locale.ENGLISH);
    private Calendar cal = Calendar.getInstance(Locale.ENGLISH);
    private Context context;
    private GridAdapter mAdapter;
    int prev=-1,pos,cr_pos=-2;
    List<Date> dayValueInCells;
    List<EventObjects> mEvents;
    Date selected;
    Calendar today_date=Calendar.getInstance();
    Date color_date;
    DateSelector mDateSelector;

    public CustomCalenderView(Context context) {
        super(context);
    }
    public CustomCalenderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        color_date=today_date.getTime();
        initializeUILayout();
        setUpCalendarAdapter();
        setPreviousButtonClickEvent();
        setNextButtonClickEvent();
        setGridCellClickEvents();


        Log.d(TAG, "I need to call this method");
    }
    public CustomCalenderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    private void initializeUILayout(){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.calendarview, this);
        previousButton = (ImageView)view.findViewById(R.id.previous_month);
        nextButton = (ImageView)view.findViewById(R.id.next_month);
        currentDate = (TextView)view.findViewById(R.id.display_current_date);
        calendarGridView = (GridView)view.findViewById(R.id.calendar_grid);


    }
    private void setPreviousButtonClickEvent(){
        previousButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                cal.add(Calendar.MONTH, -1);
                setUpCalendarAdapter();
            }
        });
    }
    private void setNextButtonClickEvent(){
        nextButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                cal.add(Calendar.MONTH, 1);
                setUpCalendarAdapter();
            }
        });
    }
    public void setGridCellClickEvents(){
        calendarGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pos=(int)view.getTag();
                view.setBackgroundResource(R.drawable.calendarview_select_date);
                TextView txt=view.findViewById(R.id.calendar_date_id);
                txt.setTextColor(Color.WHITE);
                color_date=dayValueInCells.get(pos);

                if((prev!=-1)&&prev!=cr_pos)
                {
                    parent.getChildAt(prev).setBackgroundColor(Color.parseColor("#ffffff"));
                    TextView txtd=parent.getChildAt(prev).findViewById(R.id.calendar_date_id);
                    txtd.setTextColor(Color.BLACK);
                }
                if(prev==cr_pos)
                {
                    parent.getChildAt(prev).setBackgroundResource(R.drawable.calendarview_today);
                    TextView txtd=parent.getChildAt(prev).findViewById(R.id.calendar_date_id);
                    txtd.setTextColor(Color.BLACK);
                }
                prev=pos;

                int month_id=(int)txt.getTag();
                if(month_id==-1)
                {
                   cr_pos=pos;
                }
                if(month_id==1)
                {
                    cal.add(Calendar.MONTH, 1);
                    setUpCalendarAdapter();

                    cr_pos=-2;

                }
                if(month_id==2)
                {
                    cal.add(Calendar.MONTH, -1);
                    setUpCalendarAdapter();

                    cr_pos=-2;
                }
                mDateSelector.onDateSelected();
            }
        });
    }
    private void setUpCalendarAdapter(){
        dayValueInCells = new ArrayList<Date>();
        mEvents=new ArrayList<>();
        String sDate1="27/02/2020";
        try {
            Date date1=new SimpleDateFormat("dd/MM/yyyy").parse(sDate1);

        EventObjects evd=new EventObjects(10,"hello",date1);
        EventObjects evde=new EventObjects(11,"hi",new Date("27/01/2020"));

        mEvents.add(evd);
        mEvents.add(evde);
        } catch (ParseException e) {
            Toast.makeText(getContext(),e.toString(),Toast.LENGTH_SHORT).show();
        }

        Calendar mCal = (Calendar)cal.clone();
        mCal.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfTheMonth = mCal.get(Calendar.DAY_OF_WEEK) - 1;
        mCal.add(Calendar.DAY_OF_MONTH, -firstDayOfTheMonth);
        while(dayValueInCells.size() < MAX_CALENDAR_COLUMN){
            dayValueInCells.add(mCal.getTime());
            mCal.add(Calendar.DAY_OF_MONTH, 1);
        }
        Log.d(TAG, "Number of date " + dayValueInCells.size());
        String sDate = formatter.format(cal.getTime());
        currentDate.setText(sDate);
        prev=dayValueInCells.indexOf(color_date);
        cr_pos=dayValueInCells.indexOf(today_date.getTime());
        mAdapter = new GridAdapter(context, dayValueInCells, cal, mEvents,color_date);
        calendarGridView.setAdapter(mAdapter);

    }


    interface DateSelector
    {
        void onDateSelected();
    }


}
