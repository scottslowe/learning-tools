using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Mars.Components
{
    public class StorageAppSettings
    {
        private string _storageAccount;
        public StorageAppSettings(string storageAccount) {
            _storageAccount = storageAccount;
        }

        public string TableEndPoint {
            get {
                return string.Format("https://{0}.table.core.windows.net/", _storageAccount);
            }
        }

        public string FileEndPoint {
            get {
                return string.Format("https://{0}.file.core.windows.net/", _storageAccount);
            }
        }
        /// <summary>
        /// Table parameters
        /// </summary>
        public const string CourseTable = "Courses";
        public const string EnrollmentTable = "Enrollment";
    }
}
