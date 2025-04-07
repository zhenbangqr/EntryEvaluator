package com.zhenbang.entryevaluator // Replace with your actual package name

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Define Qualification Types
enum class QualificationType {
    SPM, OLevel, UEC
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EntryRequirementScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryRequirementScreen() {
    var selectedQualification by remember { mutableStateOf(QualificationType.SPM) }
    var relevantCreditsText by remember { mutableStateOf(TextFieldValue("")) } // Use TextFieldValue for easier handling
    var englishGrade by remember { mutableStateOf(TextFieldValue("")) }
    var bmGrade by remember { mutableStateOf(TextFieldValue("")) } // Only for SPM
    var mathGrade by remember { mutableStateOf(TextFieldValue("")) }
    var evaluationResult by remember { mutableStateOf<String?>(null) } // To store the result message

    // Helper function to check if a grade meets the minimum requirement
    // Assumes grades are single uppercase letters (A, B, C, etc.)
    fun isGradeSufficient(inputGrade: String, requiredGrade: Char): Boolean {
        val grade = inputGrade.trim().uppercase()
        if (grade.isEmpty() || grade.length > 1 || !grade[0].isLetter()) {
            return false // Invalid input
        }
        // Compare alphabetically: 'A' < 'B' < 'C'
        return grade[0] <= requiredGrade
    }

    // Function to perform the evaluation
    fun evaluate() {
        val credits = relevantCreditsText.text.toIntOrNull() ?: 0 // Safely convert text to Int
        val engGradeInput = englishGrade.text
        val mathGradeInput = mathGrade.text

        val meetsRequirements = when (selectedQualification) {
            QualificationType.SPM -> {
                val bmGradeInput = bmGrade.text
                credits >= 5 &&
                        isGradeSufficient(engGradeInput, 'C') &&
                        isGradeSufficient(bmGradeInput, 'C') &&
                        isGradeSufficient(mathGradeInput, 'B')
            }
            QualificationType.OLevel -> {
                credits >= 5 &&
                        isGradeSufficient(engGradeInput, 'C') &&
                        isGradeSufficient(mathGradeInput, 'C')
            }
            QualificationType.UEC -> {
                // Note: UEC requires 3 Grade B. We are checking the *count* of B's here.
                credits >= 3 &&
                        isGradeSufficient(engGradeInput, 'C') &&
                        isGradeSufficient(mathGradeInput, 'B')
            }
        }

        evaluationResult = if (meetsRequirements) {
            "Congratulations! You meet the minimum entry requirements."
        } else {
            "Sorry, you do not meet the minimum entry requirements based on the provided details."
            // You could add more specific reasons here later
        }
    }

    // --- UI Layout ---
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("TARUMT Entry Requirement Checker") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp) // Add padding around the content
                .verticalScroll(rememberScrollState()), // Make content scrollable
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp) // Spacing between elements
        ) {

            Text("Select Qualification:", style = MaterialTheme.typography.titleMedium)

            // Radio buttons to select qualification
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                QualificationType.values().forEach { qualification ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = selectedQualification == qualification,
                            onClick = {
                                selectedQualification = qualification
                                // Clear fields when changing qualification
                                relevantCreditsText = TextFieldValue("")
                                englishGrade = TextFieldValue("")
                                bmGrade = TextFieldValue("")
                                mathGrade = TextFieldValue("")
                                evaluationResult = null // Clear previous result
                            }
                        )
                        Text(text = qualification.name, modifier = Modifier.padding(start = 4.dp, end = 16.dp))
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp)) // Visual separator

            // --- Input Fields ---

            // Label for relevant credits/grades input field changes based on selection
            val relevantCreditsLabel = when (selectedQualification) {
                QualificationType.SPM -> "Number of relevant subjects with at least Grade C"
                QualificationType.OLevel -> "Number of relevant subjects with at least Grade C"
                QualificationType.UEC -> "Number of relevant subjects with at least Grade B"
            }
            OutlinedTextField(
                value = relevantCreditsText,
                onValueChange = { relevantCreditsText = it },
                label = { Text(relevantCreditsLabel) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = englishGrade,
                onValueChange = { englishGrade = it },
                label = { Text("English Language Grade (e.g., A, B, C)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Show Bahasa Malaysia field only for SPM
            if (selectedQualification == QualificationType.SPM) {
                OutlinedTextField(
                    value = bmGrade,
                    onValueChange = { bmGrade = it },
                    label = { Text("Bahasa Malaysia Grade (e.g., A, B, C)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            val mathGradeLabel = when (selectedQualification) {
                QualificationType.OLevel -> "Mathematics Grade (e.g., A, B, C)"
                else -> "Mathematics Grade (e.g., A, B)" // SPM & UEC require B
            }
            OutlinedTextField(
                value = mathGrade,
                onValueChange = { mathGrade = it },
                label = { Text(mathGradeLabel) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp)) // Add some space before the button

            // Evaluation Button
            Button(onClick = { evaluate() }) {
                Text("Evaluate Requirements")
            }

            Spacer(modifier = Modifier.height(16.dp)) // Add some space before the result

            // Display Result
            evaluationResult?.let { result ->
                Text(
                    text = result,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (result.startsWith("Congratulations")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
            Text(
                text = "Note: This checker validates against minimum requirements. Specific 'relevant subjects' are determined by the university.",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 10.dp)
            )
            // Note about SPM consideration for 3-4 C's
            if (selectedQualification == QualificationType.SPM) {
                Text(
                    text = "Note for SPM: Applicants with 3 or 4 Grade C may still be considered (check official TARUMT guidelines).",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 5.dp)
                )
            }
        }
    }
}