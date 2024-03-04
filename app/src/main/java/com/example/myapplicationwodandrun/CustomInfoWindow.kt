import android.content.Context
import android.widget.LinearLayout
import android.widget.TextView
import com.example.myapplicationwodandrun.R
import com.example.myapplicationwodandrun.raceData
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow

class CustomInfoWindow(layoutResId: Int, mapView: MapView, private val raceData: raceData) :
    InfoWindow(layoutResId, mapView) {

    override fun onOpen(item: Any?) {
        // Personnalisez l'affichage des détails de la course ici
        val layout = mView.findViewById<LinearLayout>(R.id.bubble_layout)
        val title = mView.findViewById<TextView>(R.id.bubble_title)
        val snippet = mView.findViewById<TextView>(R.id.bubble_description)

        //title.text = "PIPICACAPROUTE" // Ajoutez le titre que vous souhaitez afficher
        snippet.text = "Date: ${raceData.date}\nDistance: ${raceData.distance} km\nTemps: ${raceData.tempsRealise}"
    }

    override fun onClose() {
        // Actions lors de la fermeture de l'infobulle
    }
}