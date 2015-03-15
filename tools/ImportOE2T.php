<?php

namespace DevEco37 ;

class ImportOE2T {

	//const OE2T_FILE = '/home/cyrille/ownCloud/taf/CoopAxis/Projets/Data/DevEco37/data/OE2T-v2-EXTRAIT1.csv';
	const OE2T_FILE = '/home/cyrille/ownCloud/taf/CoopAxis/Projets/Data/DevEco37/data/OE2T-v2.csv';
	//const CHARSET_IN = 'ISO-8859-1//TRANSLIT';
	const CHARSET_IN = 'UTF-8';
	const CHARSET_OUT = 'UTF-8';

	public function load()
	{
		$headers = array();
		$data = array();

		$file = new \SplFileObject(self::OE2T_FILE);
		$file->setFlags(\SplFileObject::READ_CSV | \SplFileObject::SKIP_EMPTY);
		$file->setCsvControl( ';', '"', '\\');

		$linesCpt = 0 ;
		foreach ($file as $row)
		{
			$linesCpt ++ ;
			if( count($row) <= 1 )
				continue ;

			if( $linesCpt == 1 )
			{
				$colIdx = 0 ;
				foreach( $row as $col )
					$headers[$col] = $colIdx ++ ;
				//echo 'HEADERS: ', var_export( $headers, true ), "\n";

				// Ajout d'une colonne pour l'adresse
				$headers['ADR'] = $colIdx ++ ;

				continue ;
			}

			$row2 = array();
			foreach( $row as $col )
			{
				$row2[] = iconv( self::CHARSET_IN, self::CHARSET_OUT, $col );
			}

			$ADR1 = trim($row[$headers['Complément d\'adresse']]) ;
			$ADR2 = trim($row[$headers['Numéro']]) ;
			$ADR3 = trim($row[$headers['Nom de la rue']]) ;
			$ADR4 = trim($row[$headers['Nom de la ZA']]) ;
			$CPVILLE = $row[$headers['Code postal']] ;
			$VILLE = $row[$headers['Nom de la commune']] ;

			$row2[] = ($ADR1==''?'':''.$ADR1).' '.($ADR2==''?'':', '.$ADR2).($ADR3==''?'':', '.$ADR3).($ADR4==''?'':', '.$ADR4).', '.$CPVILLE.' '.$VILLE ;

			$data[] = $row2 ;
		}

		return array( 'headers'=>$headers, 'rows'=>$data );
	}
}
