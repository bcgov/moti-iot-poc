using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace oahu_api.Models
{
    /// <summary>
    /// Device
    /// </summary>
    [Table("acs_device")]
    public class Device
    {
        /// <summary>
        /// ID of the device
        /// </summary>
        public long id { get; set; }

        /// <summary>
        /// Latitude of the device
        /// </summary>
        public string lat { get; set; }

        /// <summary>
        /// Longitude of the device
        /// </summary>
        public string lng { get; set; }

        /// <summary>
        /// Name of the device
        /// </summary>
        public string name { get; set; }
    }
}