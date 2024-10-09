package com.fabricdeclarative


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.facebook.react.bridge.Arguments        // 👈 add this
import com.facebook.react.bridge.WritableMap      // 👈 add this
import com.facebook.react.uimanager.events.Event  // 👈 add this


@Composable
fun JetpackComposeView(
  viewModel: JetpackComposeViewModel,
  onSubmit: (inputString: String, selectedNumber :Double, restNumbers: ArrayList<Double>) -> Unit
) {

  val title by viewModel.title.collectAsState()                   //👈 add this
  val dropdownOptions by viewModel.options.collectAsState()       //👈 add this
  val inputString by viewModel.inputString.collectAsState()       //👈 add this
  val selectedOption by viewModel.selectedOption.collectAsState() //👈 add this

  Column (
    Modifier
      .fillMaxWidth()
      .fillMaxHeight(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {

    Card (modifier = Modifier.fillMaxWidth(0.9f)) {
      Column (
        Modifier.padding(vertical = 15.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
      ) {
        var isDropdownDisplayed by remember { mutableStateOf(false) }

        Text(text = title) //👈 update this

        Row {
          TextField(
            value = inputString, //👈 update this
            onValueChange = { viewModel.updateInputString(it) },
            modifier = Modifier.weight(1f)
          )
        }


        Spacer(
          modifier = Modifier.height(15.dp)
        )

        Box (modifier = Modifier.fillMaxWidth(0.9f)) {
          TextButton (
            onClick = { isDropdownDisplayed = true }
          ) {
            val text = if (selectedOption.isNaN() || selectedOption == 0.0) "Select Number" else "Number: ${selectedOption}" //👈 add this
            Text(text = text, modifier = Modifier.weight(1f)) //👈 update this
            Icon(Icons.Default.ArrowDropDown, contentDescription = "dropdown")
          }

          DropdownMenu(
            modifier = Modifier.fillMaxWidth(0.8f),
            expanded = isDropdownDisplayed,
            onDismissRequest = { isDropdownDisplayed = false }
          ) {
            for (option in dropdownOptions) {
              TextButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                  isDropdownDisplayed = false  //👈 add this
                  viewModel.updateSelectedOption(option.toDouble()) //👈 add this
                }
              ) {
                Text("Option: ${option}")
              }
            }
          }
        }


        Spacer(
          modifier = Modifier.height(15.dp)
        )

        Button(
          onClick = {
            // 👇 add this
            val restNumbers = ArrayList(dropdownOptions.filter { it != selectedOption  })

            onSubmit(
              inputString,
              selectedOption,
              restNumbers
            )
            // 👆 add this
          }
        ) {
          Text(text = "Submit")
        }

      }
    }
  }
}

class SubmitEvent(
  surfaceId: Int,
  viewId: Int,
  val inputString: String,
  val selectedNumber: Double,
  val restNumbers: ArrayList<Double>
) : Event<SubmitEvent>(surfaceId, viewId) {
  override fun getEventName() = EVENT_NAME

  override fun getCoalescingKey(): Short = 0

  override fun getEventData(): WritableMap? = Arguments.createMap().also {

    it.putString("input", inputString)
    it.putDouble("selectedNumber", selectedNumber)

    it.putMap("objectResults", Arguments.createMap().also {map ->

      val resultNumbers = Arguments.createArray().also { arr ->
        restNumbers.forEach{ number -> arr.pushDouble(number) }
      }

      map.putArray("restNumbers", resultNumbers)
      map.putString("uppercaseInput", inputString.uppercase())
    })
  }

  companion object {
    const val EVENT_NAME = "onSubmit"
  }
}


//Optional, if you want to have preview functionallity in Android Studio
@Preview(showBackground = true)
@Composable
private fun JetpackComposeViewPreview() {
  JetpackComposeView(viewModel = JetpackComposeViewModel(),{ _, _, _ -> }) //👈 update this
}

class JetpackComposeViewModel : ViewModel() {
  private val _title = MutableStateFlow("")
  private val _options = MutableStateFlow(ArrayList<Double>())
  private val _selectedOption = MutableStateFlow(0.0)
  private val _inputString = MutableStateFlow("")


  val title: StateFlow<String> get() = _title
  val options: StateFlow<ArrayList<Double>> get() = _options
  val selectedOption: StateFlow<Double> get() = _selectedOption
  val inputString: StateFlow<String> get() = _inputString

  fun updateTitle(newTitle: String) {
    _title.value = newTitle
  }

  fun updateOptions(newOptions: ArrayList<Double>) {
    _options.value = newOptions
  }

  fun updateSelectedOption(newOption: Double) {
    _selectedOption.value = newOption
  }

  fun updateInputString(newValue: String) {
    _inputString.value = newValue
  }
}
