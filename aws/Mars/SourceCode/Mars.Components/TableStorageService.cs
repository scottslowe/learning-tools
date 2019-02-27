using Microsoft.WindowsAzure.Storage.Auth;
using Microsoft.WindowsAzure.Storage.Table;
using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Mars.Components
{
    public class TableStorageService
    {
        CloudTableClient _sasClient;
        public TableStorageService(string endPoint, string sasToken)
        {
            string courseContentEndPoint = endPoint;
            string courseSASToken = sasToken;

            StorageCredentials creds = new StorageCredentials(courseSASToken);
            _sasClient = new CloudTableClient(new Uri(courseContentEndPoint), creds);
        }

        public string GetCourses() {
            var courses = GetCoursesInternal();
            var resultJson = JsonConvert.SerializeObject(courses);
            return resultJson;
        }

        public string GetEnrollment(string email) {
            var enrollment = GetEnrollmentInternal(email);
            var resultJson = JsonConvert.SerializeObject(enrollment);
            return resultJson;
        }

        public bool IsAllowedDownload(string email, string path) {
            bool isAllowed = false;
            //get all courses
            var courses = GetCoursesInternal();

            // get course share root
            var folders = path.Split('/');
            var courseShareRoot = folders[0];

            // map share root to course
            string course = string.Empty;
            foreach (var c in courses) {
                if (c.RowKey.ToLower() == courseShareRoot.ToLower()) {
                    course = c.PartitionKey;
                    // allowed if enroll the course
                    isAllowed = IsEnrolled(email, course);
                    break;
                }
            }

            return isAllowed;
        }

        public bool IsEnrolled(string email,string course) {
            bool isEnrolled = false;
            CloudTable table = _sasClient.GetTableReference(StorageAppSettings.EnrollmentTable);

            TableQuery<TableEntity> tableQuery = new TableQuery<TableEntity>().Where(
                TableQuery.CombineFilters(
                      TableQuery.GenerateFilterCondition("PartitionKey", QueryComparisons.Equal, email),
                      TableOperators.And,
                      TableQuery.GenerateFilterCondition("RowKey", QueryComparisons.Equal, course)));

            var enrollment = table.ExecuteQuery(tableQuery).ToList();
            if (enrollment != null && enrollment.Count > 0)
            {
                isEnrolled = true;
            }

            return isEnrolled;
        }

        private List<CourseTableEntity> GetCoursesInternal() {
            CloudTable table = _sasClient.GetTableReference(StorageAppSettings.CourseTable);

            var courses = table.ExecuteQuery(new TableQuery<CourseTableEntity>()).ToList();
            return courses;
        }
        private List<TableEntity> GetEnrollmentInternal(string email) {
            CloudTable table = _sasClient.GetTableReference(StorageAppSettings.EnrollmentTable);

            var tableQuery = new TableQuery<TableEntity>().Where(
                    TableQuery.GenerateFilterCondition("PartitionKey", QueryComparisons.Equal, email)
                );

            var enrollment = table.ExecuteQuery(tableQuery).ToList();

            return enrollment;
        }
    }
}
