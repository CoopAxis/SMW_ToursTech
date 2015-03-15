#!/usr/bin/env php
<?php
/**
 * Génère sur STDOUT au format JSON une vue des codes NAF (APE) présents dans la base smw.coopaxis.fr.
 * 
 * Data Requirements:
 * - ficher des intitulés courts des NAF
 * 
 * Libraries Requirements:
 * - PHPExcel_1.8.0
 * - EasyRdf
 * 
 * Licence CC0
 * 2015 cyrille at giquello.fr
 *
 */
 
error_reporting(-1);

define( 'DATA_FILE', __DIR__.'/int_courts_naf_rev_2.xls');

require_once( __DIR__.'/PHPExcel_1.8.0/PHPExcel.php');
require_once( __DIR__.'/EasyRdf-common.php');

$usedAPE = getAPE();

//makeAPETree($usedAPE);
makeAPETree2($usedAPE);

/**
 */
function getAPE()
{

	$sparql = new \EasyRdf\Sparql\Client('http://smw.coopaxis.fr/sparql');

	//echo '===================================',"\n",'===>>> APE', "\n";

	$result = $sparql->query( RDF_QUERY_PREFIX . <<<'EOT'

		SELECT ?fiche ?label ?APE
		WHERE {
		 ?fiche rdf:type schema:Organization .
		 ?fiche rdfs:label ?label .
		 ?fiche wiki:Attribut-3ACode_APE ?APE .
		}
		ORDER BY ?fiche
EOT
	);

	//echo '===>>> Rows count = ', count($result),"\n" ;

	$apes = array();
	foreach ($result as $row) {
		//echo var_export( $row,true),"\n";
		$ape = (string) $row->APE ;
		//echo $ape, ' - ', $row->label, ' (', $row->fiche, ')', "\n";
		if( ! isset($apes[$ape]) )
			$apes[$ape] = 1 ;
		else
			$apes[$ape] ++ ;
	}

	//echo var_export( $apes, true ),"\n";
	return $apes ;
}

function makeAPETree($usedAPE)
{

	$inputFileType = PHPExcel_IOFactory::identify(DATA_FILE);
	$objReader = PHPExcel_IOFactory::createReader($inputFileType);
	$objReader->setReadDataOnly(true);
	$objPHPExcel = $objReader->load(DATA_FILE);

	$sheetData = $objPHPExcel->getActiveSheet()->toArray(null,true,true,true);
	//var_dump($sheetData);

	$cptLines = 0 ;
	$nafs = array('name'=>'NAF INSEE', 'children'=>array());
	$n1=$n2=$n3=-1;
	foreach( $sheetData as $row )
	{
		$cptLines ++ ;
		if( $cptLines<=1 )
			continue ;
		if( $row['B'] == '' )
			continue ;
		if( preg_match( '/^SECTION [A-Z]$/', $row['B'] ) )
		{
			$n1 ++ ;
			$n2 = $n3 = -1 ;
			$label = '('.$row['B'].') '.$row['E'] ;
			$nafs['children'][$n1] = array('name'=> $label, 'children'=>array()) ;
		}
		else if( preg_match( '/^[0-9]{2}$/', $row['B'] ) )
		{
			$n2 ++ ;
			$n3 = -1 ;
			$label = '('.$row['B'].') '.$row['E'] ;
			$nafs['children'][$n1]['children'][$n2] = array('name'=> $label, 'children'=>array()) ;
		}
		else if( preg_match( '/^[0-9]{2}[\.][0-9]{1}$/', $row['B'] ) )
		{
			$n3 ++ ;
			$label = '('.$row['B'].') '.$row['E'] ;
			$nafs['children'][$n1]['children'][$n2]['children'][$n3] = array('name'=> $label, 'children'=>array()) ;
		}
		else
		{
			$ape = str_replace('.','',$row['B'] );
			$size = isset($usedAPE[$ape]) ? $usedAPE[$ape] : 0 ;

			$label = '('.$row['B'].') '.$row['E'] ;
			$nafs['children'][$n1]['children'][$n2]['children'][$n3]['children'][] =
				array(
					'name'=> $label,
					'size'=> $size
				);
		}

	}

	$json = json_encode( $nafs,  JSON_UNESCAPED_UNICODE );
	echo $json ,"\n";

}

function makeAPETree2($usedAPE)
{

	$inputFileType = PHPExcel_IOFactory::identify(DATA_FILE);
	$objReader = PHPExcel_IOFactory::createReader($inputFileType);
	$objReader->setReadDataOnly(true);
	$objPHPExcel = $objReader->load(DATA_FILE);

	$sheetData = $objPHPExcel->getActiveSheet()->toArray(null,true,true,true);
	//var_dump($sheetData);

	$cptLines = 0 ;
	$nafs = array('name'=>'NAF INSEE', 'children'=>array());
	$n1=$n2=$n3=-1;
	foreach( $sheetData as $row )
	{
		$cptLines ++ ;
		if( $cptLines<=1 )
			continue ;
		if( $row['B'] == '' )
			continue ;
		if( preg_match( '/^SECTION [A-Z]$/', $row['B'] ) )
		{
			$n1 ++ ;
			$n2 = $n3 = -1 ;
			$label = '('.$row['B'].') '.$row['E'] ;
			$nafs['children'][$n1] = array('name'=> $label, 'children'=>array()) ;
		}
		else if( preg_match( '/^[0-9]{2}$/', $row['B'] ) )
		{
			$n2 ++ ;
			$n3 = -1 ;
			$label = '('.$row['B'].') '.$row['E'] ;
			$nafs['children'][$n1]['children'][$n2] = array('name'=> $label, 'children'=>array()) ;
		}
		else if( preg_match( '/^[0-9]{2}[\.][0-9]{1}$/', $row['B'] ) )
		{
			$apen3 = str_replace('.','',$row['B'] );
			$size = 0 ;
			foreach( $usedAPE as $ape => $apeCount )
			{
				if( strpos( $ape, $apen3 ) === 0 )
					$size = $apeCount ;
			}

			$n3 ++ ;
			$label = '('.$row['B'].') '.$row['E'] ;
			$nafs['children'][$n1]['children'][$n2]['children'][$n3] =
				array(
					'name'=> $label,
					'size'=>$size
				);
		}
		else
		{
		}

	}

	$json = json_encode( $nafs,  JSON_UNESCAPED_UNICODE );
	echo $json ,"\n";

}
