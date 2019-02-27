using Microsoft.VisualStudio.TestTools.UnitTesting;
using Mars.Components;
using System.Configuration;

namespace Mars.Components.Tests
{
    [TestClass()]
    public class TableStorageServiceTests
    {
        private string _endPoint;
        private string _sasToken;

        public TableStorageServiceTests()
        {
            string storageAccount = ConfigurationManager.AppSettings["CourseStorageAccount"];
            _sasToken = ConfigurationManager.AppSettings["CourseSASToken"];

            var settings = new StorageAppSettings(storageAccount);
            _endPoint = settings.TableEndPoint;

        }

        [TestMethod()]
        public void GetCoursesTest()
        {
            TableStorageService service = new TableStorageService(_endPoint, _sasToken);
            string json = service.GetCourses();
        }

        [TestMethod()]
        public void GetEnrollmentTest()
        {
            string email = "ggwd@hotmail.com";
            TableStorageService service = new TableStorageService(_endPoint, _sasToken);
            string json = service.GetEnrollment(email);
        }

        [TestMethod()]
        public void IsEnrolledYesTest()
        {
            string email = "ggwd@hotmail.com";
            string course = ".Net MCTS";
            TableStorageService service = new TableStorageService(_endPoint, _sasToken);
            bool isEnrolled = service.IsEnrolled(email, course);
            Assert.IsTrue(isEnrolled);
        }

        [TestMethod()]
        public void IsEnrolledNoTest()
        {
            string email = "ggwd@hotmail.com";
            string course = "Other Course";
            TableStorageService service = new TableStorageService(_endPoint, _sasToken);
            bool isEnrolled = service.IsEnrolled(email, course);
            Assert.IsFalse(isEnrolled);
        }

        [TestMethod()]
        public void IsAllowDownloadYesTest()
        {
            string email = "ggwd@hotmail.com";
            string path = "mcts";
            TableStorageService service = new TableStorageService(_endPoint, _sasToken);
            bool isAllowed = service.IsAllowedDownload(email, path);
            Assert.IsTrue(isAllowed);
        }

        [TestMethod()]
        public void IsAllowDownloadYes2Test()
        {
            string email = "ggwd@hotmail.com";
            string path = "mcts/demo";
            TableStorageService service = new TableStorageService(_endPoint, _sasToken);
            bool isAllowed = service.IsAllowedDownload(email, path);
            Assert.IsTrue(isAllowed);
        }

        [TestMethod()]
        public void IsAllowDownloadNoTest()
        {
            string email = "ggwd@hotmail.com";
            string path = "project";
            TableStorageService service = new TableStorageService(_endPoint, _sasToken);
            bool isAllowed = service.IsAllowedDownload(email, path);
            Assert.IsFalse(isAllowed);
        }

        [TestMethod()]
        public void IsAllowDownloadNo2Test()
        {
            string email = "ggwd@hotmail.com";
            string path = "project/course01";
            TableStorageService service = new TableStorageService(_endPoint, _sasToken);
            bool isAllowed = service.IsAllowedDownload(email, path);
            Assert.IsFalse(isAllowed);
        }
    }
}