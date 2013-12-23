package ab.vision;

public enum ABType {
	
	Hill(1),
	Sling(2),
	RedBird(3), 
	YellowBird(4), 
	BlueBird(5), 
	BlackBird(6), 
	WhiteBird(7), 
	Pig(8),
	Ice(9), 
	Wood(10), 
	Stone(11), 
	TNT(12),
	Unknown(0);
	public int id;
	private ABType(int id)
	{
		this.id = id;
	}
}
