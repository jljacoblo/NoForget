package net.jacoblo.noforget

import android.app.DatePickerDialog
import android.app.Fragment
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class MemoryEntryFragment : Fragment() {

  var m_MemoryEntry: MemoryEntry = createEmptyMemoryEntry()
  var m_DefaultReminderDates: ArrayList<LocalDateTime> = createDefaultReminderDates()

  override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
    super.onCreateView(inflater, container, savedInstanceState)
    val view: View = inflater!!.inflate(R.layout.memory_entry_fragment, container, false)

    val addDateButton = view.findViewById<Button>( R.id.memory_entry_fragment_add_date )
    addDateButton?.setOnClickListener{
      createNewDateSubView( m_MemoryEntry.reminder_dates, m_DefaultReminderDates )
    }

    val saveButton = view.findViewById<Button>( R.id.memory_entry_fragment_date )
    saveButton?.setOnClickListener{
      if (saveNewDataFromView() ) {
        updateMemoryEntryData()
      }
    }

    val deleteButton = view.findViewById<Button>( R.id.memory_entry_fragment_delete )
    deleteButton?.setOnClickListener{
      deleteMemoryEntry()
    }

    return view
  }

  // REMEMBER : Others setMessage using Bundle and goes here to handle
  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    val bundle: Bundle? = arguments
    if (bundle != null ) {
      val entryId = bundle.getInt("MemoryEntryPos")
      if ( entryId >= 0 && entryId < m_MemoryData.memory_entries.size) {
        m_MemoryEntry = m_MemoryData.memory_entries[ entryId ]
        populateView()
      }
    }
  }

  fun populateView() {
    view.findViewById<EditText>( R.id.memory_entry_fragment_name ).setText( m_MemoryEntry.entry_name )
    view.findViewById<EditText>( R.id.memory_entry_fragment_data ).setText( m_MemoryEntry.entry_data )

    for (i in 0 until m_MemoryEntry.reminder_dates.size) {
      createNewDateSubView( m_MemoryEntry.reminder_dates, m_DefaultReminderDates )
    }
  }


  private fun createNewDateSubView( dates: ArrayList<LocalDateTime>, defaultReminderDates: ArrayList<LocalDateTime> ) {
    val datesLayout = view.findViewById<LinearLayout>(R.id.memory_entry_fragment_dates_container)
    if (datesLayout == null ) return

    val numOfDates = datesLayout.childCount

    val dateTimeGroup = LinearLayout(context).apply {
      orientation = LinearLayout.HORIZONTAL
      val dateTimeGroupLL = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT)
      layoutParams = dateTimeGroupLL
    }
    datesLayout.addView(dateTimeGroup)

    var dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    val defaultNewDate = if (numOfDates < dates.size ) {
      dates[numOfDates].format(dateTimeFormatter)
    } else if( numOfDates < defaultReminderDates.size ) {
      defaultReminderDates[numOfDates].format(dateTimeFormatter)
    }else "Date"

    val newDateEditText = createCustomPickerEditText( defaultNewDate, 0.30f )
    newDateEditText.setOnClickListener{
      createDatePickerDialog(LocalDateTime.now().plusDays(1), newDateEditText).show()
    }

    dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val defaultNewTime = if (numOfDates < dates.size ) {
      dates[numOfDates].format(dateTimeFormatter)
    } else if( numOfDates < defaultReminderDates.size ) {
      defaultReminderDates[numOfDates].format(dateTimeFormatter)
    }else "Time"
    val newTimeEditText = createCustomPickerEditText( defaultNewTime, 0.30f )
    newTimeEditText.setOnClickListener{
      createTimePickerDialog(LocalDateTime.now(), newTimeEditText).show()
    }

    val newDeleteButton = Button(context).apply {
      text = "--"
      setLayoutParams(ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
      val newDeleteButtonLL : LinearLayout.LayoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 0.40f)
      layoutParams = newDeleteButtonLL

      setOnClickListener {
        datesLayout.removeView(dateTimeGroup)
      }
    }

    dateTimeGroup.addView(newDateEditText)
    dateTimeGroup.addView(newTimeEditText)
    dateTimeGroup.addView(newDeleteButton)

    fragmentManager
            .beginTransaction()
            .detach(this)
            .attach(this)
            .commit();
  }

  private fun createCustomPickerEditText( text: CharSequence, layoutWeight: Float): EditText {
    // REMEMBER : use .apply{} kotlin method to save code
    val newPickerEditText = EditText(context).apply {
      setLayoutParams(ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
      focusable = View.NOT_FOCUSABLE
      setText(text, TextView.BufferType.EDITABLE)
      setEnabled(true)
      val newPickerEditTextLL : LinearLayout.LayoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, layoutWeight)
      layoutParams = newPickerEditTextLL
    }

    return newPickerEditText
  }

  private fun createTimePickerDialog(defaultDate: LocalDateTime, timeEntry: EditText): TimePickerDialog {
    val lis = TimePickerDialog.OnTimeSetListener {
      _ : TimePicker, hourOfDay: Int, minute: Int ->
      timeEntry.setText( LocalTime.of(hourOfDay, minute, 0).toString() )
    }
    // REMEMBER REMEMBER : DON'T use applicationContext, use class variable "context" that is related to this fragment!!!
    return TimePickerDialog(context, lis, defaultDate.hour, defaultDate.minute, true)
  }

  private fun createDatePickerDialog(defaultDate: LocalDateTime, dateEntry: EditText ): DatePickerDialog {
    val lis = DatePickerDialog.OnDateSetListener {
      _ : DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
      // REMEMBER : month and day started at zero
      dateEntry.setText( LocalDate.of(year, monthOfYear +1, dayOfMonth).toString() )
    }
    // REMEMBER : month and day started at 1
    // REMEMBER REMEMBER : DON'T use applicationContext, use class variable "context" that is related to this fragment!!!
    return DatePickerDialog(context, lis, defaultDate.year, defaultDate.monthValue - 1, defaultDate.dayOfMonth - 1)
  }

  private fun updateMemoryEntryData() {
    if ( m_MemoryEntry.memory_entry_id < 0 ) {
      m_MemoryEntry.memory_entry_id = m_MemoryData.memory_entries.size;
      m_MemoryData.memory_entries.add( m_MemoryEntry )
      clearEntry()
    }
    else {
      m_MemoryData.memory_entries[ m_MemoryEntry.memory_entry_id ] = m_MemoryEntry
    }
    saveToFileOpration(context)
  }

  private fun saveNewDataFromView(): Boolean {
    val entryName = view.findViewById<EditText>(R.id.memory_entry_fragment_name).text
    val entryData = view.findViewById<EditText>(R.id.memory_entry_fragment_data).text
    if ( entryName.isEmpty() && entryData.isEmpty() ) {
      return false;
    }

    val newMemoryEntry = MemoryEntry ( m_MemoryEntry.memory_entry_id
            ,LocalDateTime.now()
            , entryName.toString()
            , ArrayList<LocalDateTime>()
            , entryData.toString() )

    val datesContainer = view.findViewById<LinearLayout>( R.id.memory_entry_fragment_dates_container )

    for ( i in 0 until datesContainer.childCount ) {

      val dateGroup = datesContainer.getChildAt(i) as LinearLayout;

      val dateEditText = dateGroup.getChildAt(0) as EditText
      val timeEditText = dateGroup.getChildAt(1) as EditText

      val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
      val reminderDate = LocalDateTime.parse( dateEditText.text.toString() + " " + timeEditText.text.toString(), dateTimeFormatter)

      newMemoryEntry.reminder_dates.add( reminderDate )
    }

    m_MemoryEntry = newMemoryEntry
    return true
  }

  private fun deleteMemoryEntry() {
    if ( m_MemoryEntry.memory_entry_id < 0 || m_MemoryEntry.memory_entry_id >= m_MemoryData.memory_entries.size ) return

    m_MemoryData.memory_entries.removeAt( m_MemoryEntry.memory_entry_id )
    m_MemoryEntry.memory_entry_id = Int.MIN_VALUE
    Toast.makeText(context, "This Memory Entry is deleted!", Toast.LENGTH_LONG).show()
  }

  private fun createDefaultReminderDates(): ArrayList<LocalDateTime> {
    val result = ArrayList<LocalDateTime>()
    result.add(LocalDateTime.now().plusDays(1))
    result.add(LocalDateTime.now().plusDays(3))
    result.add(LocalDateTime.now().plusDays(7))
    result.add(LocalDateTime.now().plusDays(30))
    result.add(LocalDateTime.now().plusDays(90))
    result.add(LocalDateTime.now().plusDays(365))
    return result
  }


  private fun clearEntry() {
    view.findViewById<EditText>(R.id.memory_entry_fragment_name).text = null
    view.findViewById<EditText>(R.id.memory_entry_fragment_data).text = null
    view.findViewById<LinearLayout>(R.id.memory_entry_fragment_dates_container).removeAllViews()
    m_MemoryEntry = createEmptyMemoryEntry()
    populateView()
  }

  private fun createEmptyMemoryEntry(): MemoryEntry {
    return MemoryEntry(Int.MIN_VALUE, LocalDateTime.now(),"", ArrayList<LocalDateTime>(), "")
  }
}
