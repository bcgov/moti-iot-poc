using System;
using System.Collections.Generic;
using System.Linq;

namespace oahu_api.Models
{
    public class EventRepository : IEventRepository
    {
        private readonly PgContext _context;

        public EventRepository(PgContext context)
        {
            _context = context;
        }

        public IEnumerable<Event> GetAllFromDevice(long id)
        {
            return _context.Events.Where(e => e.device_id == id).ToList();
        }

        public IEnumerable<Event> GetAllFromSensor(long id, string sensor)
        {
            return _context.Events.Where(e => e.device_id == id && e.sensor == sensor).ToList();
        }

        public Event GetLatestEventFromSensor(long id, string sensor)
        {
            return _context.Events.Where(e => e.device_id == id && e.sensor == sensor).Last();
        }

        public Event GetLatestEventFromDevice(long id)
        {
            return _context.Events.Where(e => e.device_id == id).Last();
        }

        public void Add(Event item)
        {
            _context.Events.Add(item);
            _context.SaveChanges();
        }

        public Event Find(long Id)
        {
            return _context.Events.FirstOrDefault(t => t.id == Id);
        }
    }
}