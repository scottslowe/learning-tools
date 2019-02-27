using System;

namespace MarsWebSite
{
    public class AppSettings
    {
        public static string GetEnvironmentVariable(string name)
        {
            return Environment.GetEnvironmentVariable(name, EnvironmentVariableTarget.Process);
        }

        public static string SiteRoot {
            get { return GetEnvironmentVariable("SiteRoot"); }
        }
        public static string IndexPage {
            get { return SiteRoot + "index.html"; }
        }

        public static string CourseStorageAccount
        {
            get { return GetEnvironmentVariable("CourseStorageAccount"); }
        }

        public static string CourseSASToken
        {
            get { return GetEnvironmentVariable("CourseSASToken"); }
        }

        public static int CourseCacheInMinutes {
            get { return int.Parse(GetEnvironmentVariable("CourseCacheInMinutes")); }
        }

        public static int FolderCacheInMinutes
        {
            get { return int.Parse(GetEnvironmentVariable("FolderCacheInMinutes")); }
        }
    }
}
