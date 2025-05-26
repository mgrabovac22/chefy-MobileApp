
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import hr.foi.rampu.chefy.MainActivity
import hr.foi.rampu.chefy.R
import hr.foi.rampu.chefy.data.db.RecipesDatabase
import hr.foi.rampu.chefy.models.recipe.Recipes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.random.Random

class PeriodicNotificationWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    private val db = RecipesDatabase.getInstance()
    private val sharedPreferences = context.getSharedPreferences("chefy_prefs", Context.MODE_PRIVATE)

    override fun doWork(): Result {
        Log.d("PeriodicNotificationWorker", "doWork called")

        runBlocking {
            val isFirstRun = sharedPreferences.getBoolean("is_first_run", true)
            Log.d("PeriodicNotificationWorker", "Is first run: $isFirstRun")

            val title = if (isFirstRun) "Welcome to Chefy!" else "Time to Cook!"
            var randomRecipe: Recipes? = null
            val message = if (isFirstRun) {
                "Dobrodošli, Chefy neiscrpno čeka vaš kulinarski duh!"
            } else {
                randomRecipe = getRandomRecipe()
                Log.d("PeriodicNotificationWorker", "Random recipe: $randomRecipe")

                if (randomRecipe != null) {
                    val minutes = (randomRecipe.makingTime ?: 0L) / 60000
                    val seconds = (randomRecipe.makingTime ?: 0L) % 60000 / 1000
                    val time = if (seconds.toInt() != 0) "$minutes minuta i $seconds sekundi" else "$minutes minuta"
                    "Pogledaj ukusni recept: ${randomRecipe.name}, samo $time kuhanja!"
                } else {
                    "Nemamo recepte trenutno, ali pratite nas za inspiraciju!"
                }
            }

            Log.d("PeriodicNotificationWorker", "Notification message: $message")
            showNotification(title, message, randomRecipe?.id)

            if (isFirstRun) {
                sharedPreferences.edit().putBoolean("is_first_run", false).apply()
            }
        }

        return Result.success()
    }

    private suspend fun getRandomRecipe(): Recipes? {
        return withContext(Dispatchers.IO) {
            val recipes = db.daoRec.getRecipes()
            if (recipes.isNotEmpty()) {
                val randomIndex = Random.nextInt(recipes.size)
                recipes[randomIndex]
            } else {
                null
            }
        }
    }

    private fun showNotification(title: String, message: String, recipeId: Int? = null) {
        val channelId = "chefy_channel"
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Chefy Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            recipeId?.let {
                putExtra("recipe_id", it)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

}
