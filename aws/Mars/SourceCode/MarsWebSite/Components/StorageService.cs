using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.IO;
using Microsoft.WindowsAzure.Storage.File; // Namespace for Azure Files
using Microsoft.WindowsAzure.Storage;
using Microsoft.WindowsAzure.Storage.Auth;
using Mars.Components;
using Newtonsoft.Json;

namespace MarsWebSite.Components
{
    public class StorageService
    {
        private StorageAppSettings _settings;
        private string _couserSASToken;

        public StorageService() {
            _settings = new StorageAppSettings(AppSettings.CourseStorageAccount);
            _couserSASToken = AppSettings.CourseSASToken;                  
        }

        public string ListFilesOrDirectories(string sharefolder)
        {
            string coureseContentEndPoint = _settings.FileEndPoint;
            FileStorageService service = new FileStorageService(coureseContentEndPoint, _couserSASToken);
            var list = service.ListFilesOrDirectories(sharefolder);
            string resultJson = service.ConvertToJson(list);
            return resultJson;
        }
        public CloudFile GetContent(string path,string fileName) {
            string coureseContentEndPoint = _settings.FileEndPoint;
            FileStorageService service = new FileStorageService(coureseContentEndPoint, _couserSASToken);
            var file = service.GetFile(path, fileName);
            return file;
        }
        public string GetCourses() {
            string tableEndPoint = _settings.TableEndPoint;
            var _tableService = new TableStorageService(tableEndPoint, _couserSASToken);
            return _tableService.GetCourses();
        }

        public string GetEnrollment(string email) {
            string tableEndPoint = _settings.TableEndPoint;
            var _tableService = new TableStorageService(tableEndPoint, _couserSASToken);
            return _tableService.GetEnrollment(email);
        }

        public bool IsAllowDownload(string email, string path) {
            string tableEndPoint = _settings.TableEndPoint;
            var _tableService = new TableStorageService(tableEndPoint, _couserSASToken);
            return _tableService.IsAllowedDownload(email, path);
        }
    }
}
