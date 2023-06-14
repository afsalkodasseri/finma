package app.bicast.finma.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import app.bicast.finma.db.models.BankBrs
import app.bicast.finma.db.models.Entry
import app.bicast.finma.db.models.Expense
import app.bicast.finma.db.models.HomeSummaryModel
import app.bicast.finma.db.models.User
import java.util.Calendar

class dbSql(context : Context) : SQLiteOpenHelper(context,"main_db",null,2) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("create table users(id integer primary key autoincrement, name text,phone text,photo blob)")
        db?.execSQL("create table entries(id integer primary key autoincrement, user_id integer,amount integer, description text,entry_date long,type text,brs integer, CONSTRAINT user_ids\n" +
                "FOREIGN KEY (user_id)\n" +
                "REFERENCES users(id)\n" +
                "ON DELETE CASCADE, constraint brs_ids foreign key (brs) references bank(id) on delete set null) ")

        db?.execSQL("create table expenses(id integer primary key autoincrement, name text,amount integer,description text,expense_date long,type text,brs integer, constraint brs_ids foreign key (brs) references bank(id) on delete set null)")
        db?.execSQL("create table bank(id integer primary key autoincrement, name text,amount integer,entry_date long,type text, monthly_type integer)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if(oldVersion < 2){
            updateDB_1_2(db)
//        }else if(oldVersion < 5){
//            updateDB_4_5(db)
        }else {
            db?.execSQL("drop table if exists users")
            db?.execSQL("drop table if exists entries")
            db?.execSQL("drop table if exists expenses")
            db?.execSQL("drop table if exists bank")
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

//    private fun updateDB_3_4(db: SQLiteDatabase?){
//        db?.execSQL("create table IF NOT EXISTS expenses(id integer primary key autoincrement, name text,amount integer,description text,expense_date long,type text)")
//    }
//
//    private fun updateDB_4_5(db: SQLiteDatabase?){
//        db?.execSQL("create table if not exists bank(id integer primary key autoincrement, name text,amount integer,entry_date long,type text)")
//        db?.execSQL("ALTER TABLE entries ADD COLUMN brs integer references bank(id) on delete set null")
//        db?.execSQL("ALTER TABLE expenses ADD COLUMN brs integer references bank(id) on delete set null")
//    }

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

        val crs = db.rawQuery("select * from expenses left join bank on brs = bank.id where expense_date between $startTime and $endTime $typeQuery order by expense_date desc",null)
        val result :ArrayList<Expense> = ArrayList()
        if(crs.moveToFirst()){
            do {
                val brs : BankBrs?
                if(crs.getString(8)==null){
                    brs = null
                }else{
                    brs = BankBrs(crs.getInt(7),crs.getString(8),crs.getInt(9),crs.getString(11),crs.getLong(10),crs.getInt(12))
                }
                result.add(
                    Expense(crs.getInt(0),crs.getString(1),crs.getInt(2),crs.getString(3),crs.getString(5),crs.getLong(4),
                    brs)
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

}