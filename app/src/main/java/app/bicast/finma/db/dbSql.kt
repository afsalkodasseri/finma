package app.bicast.finma.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import app.bicast.finma.db.models.BalanceRowItem
import app.bicast.finma.db.models.BankBrs
import app.bicast.finma.db.models.Entry
import app.bicast.finma.db.models.EntryRowItem
import app.bicast.finma.db.models.Expense
import app.bicast.finma.db.models.ExpenseGroup
import app.bicast.finma.db.models.HomeSummaryModel
import app.bicast.finma.db.models.User
import app.bicast.finma.db.models.WorkEvent
import java.util.Calendar

class dbSql(context : Context) : SQLiteOpenHelper(context,"main_db",null,5) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("create table users(id integer primary key autoincrement, name text,phone text,photo blob)")
        db?.execSQL("create table entries(id integer primary key autoincrement, user_id integer,amount integer, description text,entry_date long,type text,brs integer, CONSTRAINT user_ids\n" +
                "FOREIGN KEY (user_id)\n" +
                "REFERENCES users(id)\n" +
                "ON DELETE CASCADE, constraint brs_ids foreign key (brs) references bank(id) on delete set null) ")

        db?.execSQL("create table expenses(id integer primary key autoincrement, name text,amount integer,description text,expense_date long,type text,brs integer,group_id integer, constraint brs_ids foreign key (brs) references bank(id) on delete set null,constraint group_ids foreign key (group_id) references expense_group(id) on delete set null)")
        db?.execSQL("create table bank(id integer primary key autoincrement, name text,amount integer,entry_date long,type text, monthly_type integer)")
        db?.execSQL("create table expense_group(id integer primary key autoincrement, name text,color text,icon blob)")

        db?.execSQL("create table events_work(event_id integer primary key autoincrement,event_type text, event_description text,event_date long)")
        db?.execSQL("create table balance_entry (id integer primary key autoincrement,date long,amount int,type text)")
        db?.execSQL("create table balances (id integer primary key autoincrement,month text,amount int)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if(oldVersion < 2){
            updateDB_1_2(db)
        }else if(oldVersion < 3){
            updateDB_2_3(db)
        }else if(oldVersion < 4){
            updateDB_3_4(db)
        }else if(oldVersion < 5){
            updateDB_4_5(db)
        }else {
            db?.execSQL("drop table if exists users")
            db?.execSQL("drop table if exists entries")
            db?.execSQL("drop table if exists expenses")
            db?.execSQL("drop table if exists bank")
            db?.execSQL("drop table if exists expense_group")
            db?.execSQL("drop table if exists events_work")
            db?.execSQL("drop table if exists balance_entry")
            db?.execSQL("drop table if exists balances")
            onCreate(db)
        }
    }

    override fun onOpen(db: SQLiteDatabase?) {
        super.onOpen(db)
        db?.execSQL("PRAGMA foreign_keys=ON;")
    }

    private fun updateDB_1_2(db: SQLiteDatabase?){
        db?.execSQL("alter table bank add column monthly_type integer")
    }

    private fun updateDB_2_3(db: SQLiteDatabase?){
        db?.execSQL("create table expense_group(id integer primary key autoincrement, name text,color text,icon blob)")
        db?.execSQL("ALTER TABLE expenses ADD COLUMN group_id integer references expense_group(id) on delete set null")
    }
    private fun updateDB_3_4(db: SQLiteDatabase?){
        db?.execSQL("create table events_work(event_id integer primary key autoincrement,event_type text, event_description text,event_date long)")
    }
    private fun updateDB_4_5(db: SQLiteDatabase?){
        db?.execSQL("create table balance_entry (id integer primary key autoincrement,date long,amount int,type text)")
        db?.execSQL("create table balances (id integer primary key autoincrement,month text,amount int)")
    }
//
//    private fun updateDB_4_5(db: SQLiteDatabase?){
//        db?.execSQL("create table if not exists bank(id integer primary key autoincrement, name text,amount integer,entry_date long,type text)")
//        db?.execSQL("ALTER TABLE entries ADD COLUMN brs integer references bank(id) on delete set null")
//        db?.execSQL("ALTER TABLE expenses ADD COLUMN brs integer references bank(id) on delete set null")
//    }


    //Expense Group
    fun addExpenseGroup(group : ExpenseGroup) :Long{
        val db = this.writableDatabase;
        val  cv = ContentValues()
        cv.put("name",group.name)
        cv.put("color",group.color)
        cv.put("icon",group.icon)
        return db.insert("expense_group",null,cv)
    }

    fun updateExpenseGroup(group : ExpenseGroup) :Int{
        val db = this.writableDatabase;
        val  cv = ContentValues()
        cv.put("name",group.name)
        cv.put("color",group.color)
        cv.put("icon",group.icon)
        return db.update("expense_group",cv,"id = ?", arrayOf(group.id.toString()))
    }

    fun deleteExpenseGroup(group: ExpenseGroup) :Int{
        val db = this.writableDatabase;
        return db.delete("expense_group","id = ?", arrayOf(group.id.toString()))
    }

    fun getExpenseGroups() :ArrayList<ExpenseGroup>{
        val db = readableDatabase
        val crs = db.rawQuery("select * from expense_group",null)
        val result :ArrayList<ExpenseGroup> = ArrayList()
        if(crs.moveToFirst()){
            do {
                result.add(
                    ExpenseGroup(crs.getInt(0),crs.getString(1),crs.getString(2),crs.getBlob(3),null
                    )
                )
            }while (crs.moveToNext())
        }
        return result
    }

    //User
    fun addUser(user : User) :Long{
        val db = this.writableDatabase
        val  cv = ContentValues()
        cv.put("name",user.name)
        cv.put("phone",user.phone)
        cv.put("photo",user.photo)
        return db.insert("users",null,cv)
    }

    fun getUsers() :ArrayList<User>{
        val db = readableDatabase
        val crs = db.rawQuery("select * from users",null)
        val result :ArrayList<User> = ArrayList()
        if(crs.moveToFirst()){
            do {
                result.add(User(crs.getInt(0),crs.getString(1),crs.getString(2),crs.getBlob(3)))
            }while (crs.moveToNext())
        }
        return result
    }

    fun getUserBalances() :ArrayList<User>{
        val db = readableDatabase
        val crs = db.rawQuery("SELECT users.*,coalesce(paid,0)-coalesce(rec,0) as bal from users left join (SELECT sum(amount) as paid,user_id from entries WHERE type = 'PAID' GROUP by user_id) paid on paid.user_id = users.id left join (SELECT sum(amount) as rec,user_id from entries WHERE type = 'RECEIVED' group by user_id) receive on users.id = receive.user_id where bal!=0 order by case WHEN bal>0 then 0 else 1 end desc, bal desc",null)
        val result :ArrayList<User> = ArrayList()
        var tempUser: User
        if(crs.moveToFirst()){
            do {
                tempUser = User(crs.getInt(0),crs.getString(1),crs.getString(2),crs.getBlob(3))
                result.add(tempUser.setBalanceUser(tempUser,crs.getInt(4)))
            }while (crs.moveToNext())
        }
        return result
    }

    private fun addEntry(entry : Entry) :Long{
        val db = this.writableDatabase;
        val  cv = ContentValues()
        cv.put("user_id",entry.userId)
        cv.put("amount",entry.amount)
        cv.put("description",entry.description)
        cv.put("entry_date",entry.dateTime)
        cv.put("type",entry.type)
        cv.put("brs",entry.brs!!.id)
        return db.insert("entries",null,cv)
    }

    private fun updateEntry(entry : Entry) :Int{
        val db = this.writableDatabase;
        val  cv = ContentValues()
        cv.put("amount",entry.amount)
        cv.put("description",entry.description)
        cv.put("entry_date",entry.dateTime)
        cv.put("type",entry.type)
        cv.put("brs",entry.brs!!.id)
        return db.update("entries",cv,"id = ?", arrayOf(entry.id.toString()))
    }

    fun deleteEntry(entry : Entry) :Int{
        val db = this.writableDatabase;
        return db.delete("entries","id = ?", arrayOf(entry.id.toString()))
    }

    fun getEntries(type :Int = 0) :ArrayList<Entry>{
        val db = readableDatabase
        var typeQuery = ""
        when(type){
            1->typeQuery = " where entries.type = 'PAID'"
            2->typeQuery = " where entries.type = 'RECEIVED'"
        }
        val crs = db.rawQuery("select * from entries inner join users on entries.user_id = users.id left join bank on brs = bank.id $typeQuery order by entry_date desc",null)
        val result :ArrayList<Entry> = ArrayList()
        if(crs.moveToFirst()){
            do {
                val brs : BankBrs?
                if(crs.getString(12)==null){
                    brs = null
                }else{
                    brs = BankBrs(crs.getInt(11),crs.getString(12),crs.getInt(13),crs.getString(15),crs.getLong(14),crs.getInt(16));
                }
                result.add(
                    Entry(crs.getInt(0),crs.getInt(1),crs.getString(8),crs.getString(9),crs.getBlob(10),crs.getInt(2),crs.getString(3),crs.getString(5),crs.getLong(4),
                    brs
                )
                )
            }while (crs.moveToNext())
        }
        return result
    }

    fun getPersonEntries(user :Int = 0) :ArrayList<Entry>{
        val db = readableDatabase
        val crs = db.rawQuery("select * from entries inner join users on entries.user_id = users.id left join bank on brs = bank.id where users.id = $user order by entry_date desc",null)
        val result :ArrayList<Entry> = ArrayList()
        if(crs.moveToFirst()){
            do {
                val brs : BankBrs?
                if(crs.getString(12)==null){
                    brs = null
                }else{
                    brs = BankBrs(crs.getInt(11),crs.getString(12),crs.getInt(13),crs.getString(15),crs.getLong(14),crs.getInt(16));
                }
                result.add(
                    Entry(crs.getInt(0),crs.getInt(1),crs.getString(8),crs.getString(9),crs.getBlob(10),crs.getInt(2),crs.getString(3),crs.getString(5),crs.getLong(4),
                    brs
                )
                )
            }while (crs.moveToNext())
        }
        return result
    }

    fun addNewEntry(entry : Entry){
        if(entry.id == null) {
            val brsId = addBrs(entry.brs!!)
            entry.brs!!.id = brsId.toInt()
            if (entry.userId == null) {
                val userId = addUser(entry.getUserCopy())
                entry.userId = userId.toInt()
                addEntry(entry)
            } else {
                addEntry(entry)
            }
        }else{
            updateBrs(entry.brs!!)
            updateEntry(entry)
        }
    }

    //Expenses

    fun upsertExpense(entry : Expense){
        if(entry.id == null){
            val brsId = addBrs(entry.brs!!)
            entry.brs!!.id = brsId.toInt()
            addExpense(entry)
        }else{
            updateBrs(entry.brs!!)
            updateExpense(entry)
        }
    }

    fun addExpense(entry : Expense) :Long{
        val db = this.writableDatabase;
        val  cv = ContentValues()
        cv.put("name",entry.name)
        cv.put("amount",entry.amount)
        cv.put("description",entry.description)
        cv.put("expense_date",entry.dateTime)
        cv.put("type",entry.type)
        cv.put("brs",entry.brs!!.id)
        cv.put("group_id",entry.group_id)
        return db.insert("expenses",null,cv)
    }

    fun updateExpense(entry : Expense) :Int{
        val db = this.writableDatabase;
        val  cv = ContentValues()
        cv.put("name",entry.name)
        cv.put("amount",entry.amount)
        cv.put("description",entry.description)
        cv.put("expense_date",entry.dateTime)
        cv.put("type",entry.type)
        cv.put("brs",entry.brs!!.id)
        cv.put("group_id",entry.group_id)
        return db.update("expenses",cv,"id = ?", arrayOf(entry.id.toString()))
    }

    fun deleteExpense(entry : Expense) :Int{
        val db = this.writableDatabase;
        return db.delete("expenses","id = ?", arrayOf(entry.id.toString()))
    }

    fun getExpenseMonth(type :Int = 0,startTime: Long,endTime: Long) :ArrayList<Expense>{
        val db = readableDatabase
        var typeQuery = ""
        when(type){
            1->typeQuery = " and expenses.type = 'EXPENSE'"
            2->typeQuery = " and expenses.type = 'INCOME'"
        }

        val crs = db.rawQuery("select *,date(expense_date/1000,'unixepoch','localtime') as date from expenses left join bank on brs = bank.id where expense_date between $startTime and $endTime $typeQuery order by date desc, id desc",null)
        val result :ArrayList<Expense> = ArrayList()
        if(crs.moveToFirst()){
            do {
                val brs : BankBrs?
                if(crs.getString(8)==null){
                    brs = null
                }else{
                    brs = BankBrs(crs.getInt(8),crs.getString(9),crs.getInt(10),crs.getString(12),crs.getLong(11),crs.getInt(13))
                }
                result.add(
                    Expense(crs.getInt(0),crs.getString(1),crs.getInt(2),crs.getString(3),crs.getString(5),crs.getLong(4),
                    brs,crs.getString(7))
                )
            }while (crs.moveToNext())
        }
        return result
    }

    fun getExpenseMonthGrouped(startTime: Long,endTime: Long) :ArrayList<ExpenseGroup>{
        val db = readableDatabase
        val crs = db.rawQuery("select sum(amount),expense_group.* from expenses left join expense_group on group_id = expense_group.id where expense_date between $startTime and $endTime and  expenses.type='EXPENSE' GROUP by group_id order by expense_date desc",null)
        val result :ArrayList<ExpenseGroup> = ArrayList()
        if(crs.moveToFirst()){
            do {
                result.add(
                    ExpenseGroup(crs.getInt(1),crs.getString(2),crs.getString(3),crs.getBlob(4),crs.getInt(0))
                )
            }while (crs.moveToNext())
        }
        return result
    }

    fun getExpenseAllMonthGrouped(startTime: Long,endTime: Long) :ArrayList<Expense>{
        val db = readableDatabase
        val crs = db.rawQuery("select * from expenses left join bank on brs = bank.id left join expense_group on group_id = expense_group.id where expense_date between $startTime and $endTime and  expenses.type='EXPENSE' order by expense_date desc",null)
        val result :ArrayList<Expense> = ArrayList()
        if(crs.moveToFirst()){
            do {
                val brs : BankBrs?
                if(crs.getString(8)==null){
                    brs = null
                }else{
                    brs = BankBrs(crs.getInt(8),crs.getString(9),crs.getInt(10),crs.getString(12),crs.getLong(11),crs.getInt(13))
                }
                result.add(
                    Expense(crs.getInt(0),crs.getString(1),crs.getInt(2),crs.getString(3),crs.getString(5),crs.getLong(4),
                        brs,crs.getString(7))
                )
            }while (crs.moveToNext())
        }
        return result
    }


    //BRS

    fun addBrs(entry : BankBrs) :Long{
        val db = this.writableDatabase;
        val  cv = ContentValues()
        cv.put("name",entry.name)
        cv.put("amount",entry.amount)
        cv.put("entry_date",entry.dateTime)
        cv.put("type",entry.type)
        return db.insert("bank",null,cv)
    }

    fun updateBrs(entry : BankBrs) :Int{
        val db = this.writableDatabase;
        val  cv = ContentValues()
        cv.put("name",entry.name)
        cv.put("amount",entry.amount)
        cv.put("entry_date",entry.dateTime)
        cv.put("type",entry.type)
        cv.put("monthly_type",entry.monthlyIncome)
        return db.update("bank",cv,"id = ?", arrayOf(entry.id.toString()))
    }

    fun deleteBrs(entry : BankBrs) :Int{
        val db = this.writableDatabase;
        return db.delete("bank","id = ?", arrayOf(entry.id.toString()))
    }

    fun getBrsMonth(timeMonth :Calendar, startTime: Long, endTime: Long) :ArrayList<BankBrs>{
        val db = readableDatabase

        val crs = db.rawQuery("select * from bank where entry_date between $startTime and $endTime order by entry_date desc",null)
        val result :ArrayList<BankBrs> = ArrayList()
        if(crs.moveToFirst()){
            do {
                result.add(BankBrs(crs.getInt(0),crs.getString(1),crs.getInt(2),crs.getString(4),crs.getLong(3),crs.getInt(5)))
            }while (crs.moveToNext())
        }
        return result
    }

    fun getHomeSummary(startTime: Long = 0, endTime: Long = System.currentTimeMillis()) :HomeSummaryModel{
        val db = readableDatabase
        val homeSummaryModel = HomeSummaryModel()
        //for cash summary
        val crCashMonth = db.rawQuery("SELECT sum(amount) as balance,type from bank WHERE entry_date BETWEEN $startTime and $endTime group by type order by balance desc",null)
        if(crCashMonth.moveToFirst()){
            homeSummaryModel.cashAmount = crCashMonth.getInt(0)
            homeSummaryModel.cashType = crCashMonth.getString(1)
        }
        val crCashTotal = db.rawQuery("SELECT sum(amount) as balance,type from bank where amount>0 and monthly_type == '1' and entry_date BETWEEN $startTime and $endTime group by type order by balance desc",null)
        if(crCashTotal.moveToFirst()){
            homeSummaryModel.totalCash = crCashTotal.getInt(0)
            if(crCashTotal.moveToNext())
                homeSummaryModel.totalCash = homeSummaryModel.totalCash + crCashTotal.getInt(0)
        }
        val crCashBalance = db.rawQuery("SELECT sum(amount) as balance,type from bank where entry_date BETWEEN $startTime and $endTime order by balance desc",null)
        if(crCashBalance.moveToFirst()){
            homeSummaryModel.balance = crCashBalance.getInt(0)
        }
        //for expense summary
        val crExpense = db.rawQuery("SELECT sum(amount) as balance,type,count(amount) from expenses WHERE expense_date BETWEEN $startTime and $endTime and type='EXPENSE'",null)
        if(crExpense.moveToFirst()){
            homeSummaryModel.expenseAmount = crExpense.getInt(0)
            homeSummaryModel.expenseCount = crExpense.getInt(2)
        }
        //for individual summary
        val crPeople = db.rawQuery("SELECT count(id) from users",null)
        if(crPeople.moveToFirst()){
            homeSummaryModel.peopleCount = crPeople.getInt(0)
        }
        //for debts summary
        val crDebts = db.rawQuery("SELECT count(id) as count, type from entries GROUP by type order by count desc",null)
        if(crDebts.moveToFirst()){
            homeSummaryModel.debtCount = crDebts.getInt(0)
            homeSummaryModel.debtType = crDebts.getString(1)
        }
        val crPureDebts = db.rawQuery("SELECT sum(amount), type from entries WHERE entry_date BETWEEN $startTime and $endTime and type = 'PAID'",null)
        if(crPureDebts.moveToFirst()){
            homeSummaryModel.debtAmount = crPureDebts.getInt(0)
        }
        val crDebtCalculated = db.rawQuery("SELECT sum(bal) from (SELECT users.*,coalesce(paid,0)-coalesce(rec,0) as bal from users left join (SELECT sum(amount) as paid,user_id from entries WHERE type = 'PAID' and entry_date BETWEEN $startTime and $endTime GROUP by user_id) paid on paid.user_id = users.id left join (SELECT sum(amount) as rec,user_id from entries WHERE type = 'RECEIVED'  and entry_date BETWEEN $startTime and $endTime group by user_id) receive on users.id = receive.user_id) userBal",null)
        if(crDebtCalculated.moveToFirst()){
            homeSummaryModel.pureDebtAmount = crDebtCalculated.getInt(0)
        }

        return homeSummaryModel
    }

    //work events

    fun addWorkEvent(item: WorkEvent) :Long{
        val db = this.writableDatabase;
        val  cv = ContentValues()
        cv.put("event_type",item.type)
        cv.put("event_description",item.description)
        cv.put("event_date",item.date)
        return db.insert("events_work",null,cv)
    }

    fun updateWorkEvent(item : WorkEvent) :Int{
        val db = this.writableDatabase;
        val  cv = ContentValues()
        cv.put("event_type",item.type)
        cv.put("event_description",item.description)
        cv.put("event_date",item.date)
        return db.update("events_work",cv,"event_id = ?", arrayOf(item.id.toString()))
    }

    fun deleteWorkEvent(item : WorkEvent) :Int{
        val db = this.writableDatabase;
        return db.delete("events_work","event_id = ?", arrayOf(item.id.toString()))
    }

    fun getWorkEvent(startTime: Long, endTime: Long) :ArrayList<WorkEvent>{
        val db = readableDatabase
        val crs = db.rawQuery("select * from events_work where event_date between $startTime and $endTime",null)
        val result :ArrayList<WorkEvent> = ArrayList()
        if(crs.moveToFirst()){
            do {
                result.add(WorkEvent(crs.getInt(0),crs.getString(1),crs.getString(2),crs.getLong(3)))
            }while (crs.moveToNext())
        }
        return result
    }
    fun getWorkEvent(startTime: Long, endTime: Long, type :String) :ArrayList<WorkEvent>{
        val db = readableDatabase
        val crs = db.rawQuery("select * from events_work where event_date between $startTime and $endTime and event_type = '$type'",null)
        val result :ArrayList<WorkEvent> = ArrayList()
        if(crs.moveToFirst()){
            do {
                result.add(WorkEvent(crs.getInt(0),crs.getString(1),crs.getString(2),crs.getLong(3)))
            }while (crs.moveToNext())
        }
        return result
    }


    fun insertEntry(entry: EntryRowItem) {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put("date", entry.date)
        cv.put("amount", entry.amount)
        cv.put("type", entry.type)
        db.insert("balance_entry", null, cv)
    }

    fun updateEntry(entry: EntryRowItem): Int {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put("date", entry.date)
        cv.put("amount", entry.amount)
        cv.put("type", entry.type)
        return db.update("balance_entry", cv, "date=?", arrayOf(entry.date.toString()))
    }

    fun upsertEntry(entry: EntryRowItem) {
        if (updateEntry(entry) == 0) insertEntry(entry)
    }

    fun upsertEntries(entries: List<EntryRowItem>) {
        for (i in entries.indices) {
            upsertEntry(entries[i])
        }
    }

    fun getMinBalEntries(startTime: Long, endTime: Long): List<EntryRowItem> {
        val result: ArrayList<EntryRowItem> = ArrayList()
        val db = this.readableDatabase
        val crs = db.rawQuery("select * from balance_entry where date between $startTime and $endTime", null)
        try {
            if (crs.moveToFirst()) {
                do {
                    result.add(EntryRowItem(crs.getLong(1), crs.getInt(2), crs.getString(3)))
                } while (crs.moveToNext())
            }
        } catch (e: Exception) {
            Log.d("EXE", e.toString())
        }
        return result
    }

    fun getBalanceForMonth(startTime: Long, endTime: Long): Int {
        var result = 0
        val db = this.readableDatabase
        val crs = db.rawQuery("select sum(amount) from balance_entry where date between $startTime and $endTime", null)
        try {
            if (crs.moveToFirst()) {
                result = crs.getInt(0)
            }
        } catch (e: Exception) {
            Log.d("EXE", e.toString())
        }
        return result
    }

    fun insertBalance(month: Long, balance: String?) {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put("month", month)
        cv.put("amount", balance)
        db.insert("balances", null, cv)
    }

    fun updateBalance(month: Long, balance: String?): Int {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put("month", month)
        cv.put("amount", balance)
        return db.update("balances", cv, "month=?", arrayOf(month.toString()))
    }

    fun upsertBalance(month: Long, balance: String?) {
        if (updateBalance(month, balance) == 0) insertBalance(month, balance)
    }

    fun getBalances(month: String): List<BalanceRowItem> {
        val result: ArrayList<BalanceRowItem> = ArrayList()
        val db = this.readableDatabase
        val crs = db.rawQuery("select * from balances where month = '$month'", null)
        try {
            if (crs.moveToFirst()) {
                do {
                    result.add(BalanceRowItem(crs.getLong(1), crs.getString(2)))
                } while (crs.moveToNext())
            }
        } catch (e: Exception) {
            Log.d("EXE", e.toString())
        }
        return result
    }

}