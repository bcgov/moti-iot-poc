using System;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace oahu_api.Models
{
    /// <summary>
    /// Event
    /// </summary>
    [Table("acs_device_event")]
    public class Event
    {
        /// <summary>
        /// ID of the event
        /// </summary>
        public long id { get; set; }

        /// <summary>
        /// ID of the device associated with the event
        /// </summary>
        public long device_id { get; set; }

        /// <summary>
        /// Sensor associated with the event
        /// </summary>
        public string sensor { get; set; }

        /// <summary>
        /// Value of the event
        /// </summary>
        public string val { get; set; }

        /// <summary>
        /// Value of the event
        /// </summary>
        public DateTime ts { get; set; }
    }
}