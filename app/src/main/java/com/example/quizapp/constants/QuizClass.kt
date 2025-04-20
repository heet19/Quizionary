package com.example.quizapp.constants

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.quizapp.activities.QuizActivity
import com.example.quizapp.models.Category
import com.example.quizapp.models.QuestionStats
import com.example.quizapp.models.QuizQuestion
import com.example.quizapp.models.QuizResponse
import com.example.quizapp.retrofit.QuestionStatsService
import com.example.quizapp.retrofit.QuizService
import com.example.quizapp.utils.Utils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class QuizClass(private val context: Context) {

    fun getQuizList(amount:Int, category: Int?, difficulty:String?, type:String?) {
        if (Constants.isNetworkAvailable(context)) {

            val pdDialog = Utils.showProgressBar(context)

            val retrofit:Retrofit = Retrofit.Builder()
                .baseUrl("https://opentdb.com/")
                .addConverterFactory(GsonConverterFactory.create()).build()

            val service:QuizService = retrofit.create(QuizService::class.java)

            val dataCall: Call<QuizResponse> = service.getQuiz(amount, category, difficulty, type)

            dataCall.enqueue(object : Callback<QuizResponse> {
                override fun onResponse(call: Call<QuizResponse>, response: Response<QuizResponse>) {
                    pdDialog.cancel()
                    if (response.isSuccessful) {
                        val responseData:QuizResponse = response.body()!!
                        val questionList = ArrayList(responseData.results)
                        if (questionList.isNotEmpty()) {
                            val intent = Intent(context, QuizActivity::class.java)
                            intent.putExtra("questionList", questionList)
                            context.startActivity(intent)
                        } else {
                            Utils.showToast(context, "We are sorry, but currently " + "we don't have $amount question for this particular selection")
                        }
                    } else {
                        Utils.showToast(context, "Response Failed")
                    }
                }

                override fun onFailure(call: Call<QuizResponse>, t: Throwable) {
                    pdDialog.cancel()
                    Utils.showToast(context, "Failure in response")
                }
            })

        } else{
            Utils.showToast(context, "network is not available")
        }
    }

    fun getQuestionStatsList(callBack:QuestionStatCallback){
        if (Constants.isNetworkAvailable(context)) {
            val pbDialog = Utils.showProgressBar(context)
            val retrofit:Retrofit = Retrofit.Builder()
                .baseUrl("https://opentdb.com/")
                .addConverterFactory(GsonConverterFactory.create()).build()

            val service:QuestionStatsService = retrofit.create(QuestionStatsService::class.java)
            val dataCall:Call<QuestionStats> = service.getData()

            dataCall.enqueue(object : Callback<QuestionStats>{
                override fun onResponse(call: Call<QuestionStats>, response: Response<QuestionStats>) {
                    pbDialog.cancel()
                    if (response.isSuccessful) {
                        val questionStats:QuestionStats = response.body()!!
                        val categoryMap = questionStats.categories
                        callBack.onQuestionStatFetched(categoryMap)

                    } else {
                        Utils.showToast(context, "Error Code: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<QuestionStats>, t: Throwable) {
                    pbDialog.cancel()
                    Utils.showToast(context, "API Call Failure")
                }

            })
        } else {
            Utils.showToast(context, "Network is Not Available")
        }
    }

    interface QuestionStatCallback{
        fun onQuestionStatFetched(map:Map<String,Category>)
    }
}