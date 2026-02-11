package io.github.ikafire.reforge.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.ikafire.reforge.core.database.ReforgeDatabase
import io.github.ikafire.reforge.core.database.entity.TemplateFolderEntity
import io.github.ikafire.reforge.core.database.entity.TemplateExerciseEntity
import io.github.ikafire.reforge.core.database.entity.WorkoutTemplateEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TemplateDaoTest {

    private lateinit var db: ReforgeDatabase
    private lateinit var templateDao: TemplateDao
    private lateinit var templateExerciseDao: TemplateExerciseDao
    private lateinit var folderDao: TemplateFolderDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, ReforgeDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        templateDao = db.templateDao()
        templateExerciseDao = db.templateExerciseDao()
        folderDao = db.templateFolderDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndRetrieveTemplate() = runTest {
        templateDao.insertTemplate(WorkoutTemplateEntity(id = "t1", name = "Push Day"))

        val result = templateDao.getTemplateById("t1").first()
        assertEquals("Push Day", result?.name)
    }

    @Test
    fun getAllTemplatesReturnsSortedBySortOrder() = runTest {
        templateDao.insertTemplate(WorkoutTemplateEntity(id = "t1", name = "C", sortOrder = 2))
        templateDao.insertTemplate(WorkoutTemplateEntity(id = "t2", name = "A", sortOrder = 0))
        templateDao.insertTemplate(WorkoutTemplateEntity(id = "t3", name = "B", sortOrder = 1))

        val all = templateDao.getAllTemplates().first()
        assertEquals(listOf("A", "B", "C"), all.map { it.name })
    }

    @Test
    fun getTemplatesByFolderFiltersCorrectly() = runTest {
        folderDao.insertFolder(TemplateFolderEntity(id = "f1", name = "PPL"))
        templateDao.insertTemplate(WorkoutTemplateEntity(id = "t1", name = "Push", folderId = "f1"))
        templateDao.insertTemplate(WorkoutTemplateEntity(id = "t2", name = "Unfiled"))

        val inFolder = templateDao.getTemplatesByFolder("f1").first()
        assertEquals(1, inFolder.size)
        assertEquals("Push", inFolder[0].name)
    }

    @Test
    fun getTemplatesWithoutFolderReturnsOnlyUnfiled() = runTest {
        folderDao.insertFolder(TemplateFolderEntity(id = "f1", name = "PPL"))
        templateDao.insertTemplate(WorkoutTemplateEntity(id = "t1", name = "Push", folderId = "f1"))
        templateDao.insertTemplate(WorkoutTemplateEntity(id = "t2", name = "Unfiled"))

        val unfiled = templateDao.getTemplatesWithoutFolder().first()
        assertEquals(1, unfiled.size)
        assertEquals("Unfiled", unfiled[0].name)
    }

    @Test
    fun deleteTemplateRemovesIt() = runTest {
        templateDao.insertTemplate(WorkoutTemplateEntity(id = "t1", name = "Push"))
        templateDao.deleteTemplate("t1")

        assertNull(templateDao.getTemplateById("t1").first())
    }

    @Test
    fun deleteTemplateCascadesToExercises() = runTest {
        templateDao.insertTemplate(WorkoutTemplateEntity(id = "t1", name = "Push"))
        templateExerciseDao.insertTemplateExercise(
            TemplateExerciseEntity(id = "te1", templateId = "t1", exerciseId = "ex1", sortOrder = 0)
        )

        templateDao.deleteTemplate("t1")

        assertNull(templateExerciseDao.getTemplateExerciseById("te1").first())
    }

    @Test
    fun deleteFolderSetsFolderIdToNull() = runTest {
        folderDao.insertFolder(TemplateFolderEntity(id = "f1", name = "PPL"))
        templateDao.insertTemplate(WorkoutTemplateEntity(id = "t1", name = "Push", folderId = "f1"))

        folderDao.deleteFolder("f1")

        val template = templateDao.getTemplateById("t1").first()
        assertNull(template?.folderId) // SET_NULL behavior
    }

    @Test
    fun templateExercisesReturnSortedBySortOrder() = runTest {
        templateDao.insertTemplate(WorkoutTemplateEntity(id = "t1", name = "Push"))
        templateExerciseDao.insertTemplateExercise(
            TemplateExerciseEntity(id = "te2", templateId = "t1", exerciseId = "ex2", sortOrder = 1)
        )
        templateExerciseDao.insertTemplateExercise(
            TemplateExerciseEntity(id = "te1", templateId = "t1", exerciseId = "ex1", sortOrder = 0)
        )

        val exercises = templateExerciseDao.getTemplateExercises("t1").first()
        assertEquals(listOf("te1", "te2"), exercises.map { it.id })
    }

    @Test
    fun templateExerciseTargetSetsAndRepsDefaults() = runTest {
        templateDao.insertTemplate(WorkoutTemplateEntity(id = "t1", name = "Push"))
        templateExerciseDao.insertTemplateExercise(
            TemplateExerciseEntity(id = "te1", templateId = "t1", exerciseId = "ex1", sortOrder = 0)
        )

        val te = templateExerciseDao.getTemplateExerciseById("te1").first()!!
        assertEquals(3, te.targetSets)
        assertEquals(10, te.targetReps)
    }

    @Test
    fun bulkInsertTemplateExercises() = runTest {
        templateDao.insertTemplate(WorkoutTemplateEntity(id = "t1", name = "Full Body"))
        val exercises = (1..5).map {
            TemplateExerciseEntity(id = "te$it", templateId = "t1", exerciseId = "ex$it", sortOrder = it - 1)
        }
        templateExerciseDao.insertTemplateExercises(exercises)

        val result = templateExerciseDao.getTemplateExercises("t1").first()
        assertEquals(5, result.size)
    }
}
