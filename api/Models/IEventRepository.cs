using System.Collections.Generic;

namespace oahu_api.Models
{
    public interface IEventRepository
    {
        void Add(Event item);

        IEnumerable<Event> GetAllFromDevice(long key);

        IEnumerable<Event> GetAllFromSensor(long key, string sensor);

        Event GetLatestEventFromSensor(long id, string sensor);

        Event GetLatestEventFromDevice(long id);

        Event Find(long key);

    }
}