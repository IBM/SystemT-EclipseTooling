create view <ViewName> as
extract part_of_speech <list of part of speech specification> 
	with language '<language code>'
    on R.<input column> as match
from <input view> R;
