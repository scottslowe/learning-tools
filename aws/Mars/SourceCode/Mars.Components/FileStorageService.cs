using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Microsoft.WindowsAzure.Storage.File; // Namespace for Azure Files
using Microsoft.WindowsAzure.Storage.Auth;
using Newtonsoft.Json;
using System.Globalization;

namespace Mars.Components
{
    public class FileStorageService
    {
        CloudFileClient _sasClient;
        
        public FileStorageService(string endPoint,string sasToken)
        {
            string courseContentEndPoint = endPoint;
            string courseSASToken = sasToken;

            StorageCredentials creds = new StorageCredentials(courseSASToken);
            _sasClient = new CloudFileClient(new Uri(courseContentEndPoint), creds);
        }

        public object[] ListFilesOrDirectories(string shareFolder)
        {           
            var share = _sasClient.GetShareReference(shareFolder);
            var rootDirectory = share.GetRootDirectoryReference();
            var dirs = rootDirectory.ListFilesAndDirectories( );  
            
            var list = dirs.ToList<IListFileItem>();

            string type = string.Empty;
            string size = string.Empty;
            string name = string.Empty;
            string path = string.Empty;
            string isRecent = string.Empty;

            string lastModifiedDate = string.Empty;

            object[] content = new object[list.Count];
            int i = 0;
            foreach (var item in list)
            {
                if (item is CloudFileDirectory)
                {
                    var directory = ((CloudFileDirectory)item);
                    directory.FetchAttributes();
                    type = "directory";
                    name =  directory.Name;
                    path = shareFolder.Replace("/","-") + "-" + name;
                    lastModifiedDate = directory.Properties.LastModified.HasValue ?
                        directory.Properties.LastModified.Value.ToString("yyyy-MM-dd", CultureInfo.InvariantCulture) : string.Empty;
                    if (lastModifiedDate != string.Empty)
                    {
                        isRecent = IsRecentlyUpdated(directory.Properties.LastModified.Value).ToString();
                    }
                }
                else {
                    var file = ((CloudFile)item);
                    file.FetchAttributes();
                    type = "file";
                    name = file.Name;
                    path = shareFolder.Replace("/", "-");
                    size = file.Properties.Length.ToByteSize();
                    lastModifiedDate = file.Properties.LastModified.HasValue ? 
                        file.Properties.LastModified.Value.ToString("yyyy-MM-dd", CultureInfo.InvariantCulture) : string.Empty ;
                    if (lastModifiedDate != string.Empty) {
                        isRecent = IsRecentlyUpdated(file.Properties.LastModified.Value).ToString();
                    }
                }

                content[i++] = new
                {
                    Name = name,
                    Type = type,
                    LastModifiedDate = lastModifiedDate,
                    Size = size,
                    Path = path,
                    IsRecent = isRecent                    
                };
            }
            return content;
        }

        private bool IsRecentlyUpdated(DateTimeOffset dt) {
            bool isRecently= false;

            if (dt.AddDays(14) > DateTime.Now) {
                isRecently = true;
            }

            return isRecently;
        }

        public CloudFile GetFile(string shareFolder, string fileName) {
           var share = _sasClient.GetShareReference(shareFolder);
           var dir = share.GetRootDirectoryReference();
           var file = dir.GetFileReference(fileName);
            return file;
        }

        public string ConvertToJson(object objects) {
            var resultJson = JsonConvert.SerializeObject(objects);
            return resultJson;
        }
    }
}
